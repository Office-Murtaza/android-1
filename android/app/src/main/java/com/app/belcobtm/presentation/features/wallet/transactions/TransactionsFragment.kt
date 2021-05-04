package com.app.belcobtm.presentation.features.wallet.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DividerItemDecoration
import com.app.belcobtm.R
import com.app.belcobtm.data.rest.wallet.request.PriceChartPeriod
import com.app.belcobtm.databinding.FragmentTransactionsBinding
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.domain.wallet.item.ChartChangesColor
import com.app.belcobtm.domain.wallet.item.ChartDataItem
import com.app.belcobtm.presentation.core.extensions.*
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.app.belcobtm.presentation.features.wallet.transactions.TransactionsFABType.*
import com.app.belcobtm.presentation.features.wallet.transactions.adapter.TransactionsAdapter
import com.app.belcobtm.presentation.features.wallet.transactions.item.CurrentChartInfo
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import io.github.kobakei.materialfabspeeddial.FabSpeedDialMenu
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class TransactionsFragment : BaseFragment<FragmentTransactionsBinding>() {
    private val viewModel: TransactionsViewModel by viewModel {
        parametersOf(TransactionsFragmentArgs.fromBundle(requireArguments()).coinCode)
    }

    private val adapter: TransactionsAdapter = TransactionsAdapter {
        val transactionId = if (it.id.isBlank()) it.dbId else it.id
        navigate(
            TransactionsFragmentDirections.toTransactionDetailsFragment(
                viewModel.coinCode,
                transactionId
            )
        )
    }
    override val isToolbarEnabled: Boolean = true
    override val isHomeButtonEnabled: Boolean = true
    override var isMenuEnabled: Boolean = true
    override val isFirstShowContent: Boolean = false
    override val retryListener: View.OnClickListener = View.OnClickListener {
        viewModel.updateData()
    }

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentTransactionsBinding =
        FragmentTransactionsBinding.inflate(inflater, container, false)

    private fun initFabMenu() {
        val menu = FabSpeedDialMenu(requireContext())
        addButtonToMenu(menu, WITHDRAW)
        addButtonToMenu(menu, DEPOSIT)
        addButtonToMenu(menu, RECALL)
        addButtonToMenu(menu, RESERVE)
        binding.fabListView.setMenu(menu)
    }


    override fun initToolbar() {
        baseBinding.toolbarView.hide()
        (activity as AppCompatActivity).setSupportActionBar(binding.customToolbarView)
    }

    private fun addButtonToMenu(menu: Menu, buttonType: TransactionsFABType) {
        menu
            .add(Menu.FIRST, buttonType.id, buttonType.ordinal, buttonType.resText)
            .setIcon(buttonType.resIcon)
    }

    override fun FragmentTransactionsBinding.initViews() {
        setToolbarTitle(LocalCoinType.valueOf(viewModel.coinCode).fullName)
        initFabMenu()
        initChart()
        listView.adapter = adapter
        ContextCompat.getDrawable(listView.context, R.drawable.bg_divider)?.let {
            val dividerItemDecoration =
                DividerItemDecoration(listView.context, DividerItemDecoration.VERTICAL)
            dividerItemDecoration.setDrawable(it)
            listView.addItemDecoration(dividerItemDecoration)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.observeTransactions()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.disposeTransactions()
    }

    override fun FragmentTransactionsBinding.initListeners() {
        chartChipGroupView.setOnCheckedChangeListener { _, checkedId ->
            viewModel.changeCurrentTypePeriod(checkedId)
        }
        fabListView.addOnMenuItemClickListener { _, _, itemId ->
            when (itemId) {
                STAKING.id -> navigate(TransactionsFragmentDirections.toStakingFragment())
                WITHDRAW.id ->
                    navigate(TransactionsFragmentDirections.toWithdrawFragment(viewModel.coinCode))
                DEPOSIT.id ->
                    navigate(TransactionsFragmentDirections.toDepositFragment(viewModel.coinCode))
                RECALL.id ->
                    navigate(TransactionsFragmentDirections.toRecallFragment(viewModel.coinCode))
                RESERVE.id ->
                    navigate(TransactionsFragmentDirections.toReserveFragment(viewModel.coinCode))
            }
        }
    }

    override fun FragmentTransactionsBinding.initObservers() {
        viewModel.chartLiveData.observe(viewLifecycleOwner) { loadingData ->
            when (loadingData) {
                is LoadingData.Loading<CurrentChartInfo> -> {
                    chartProgressView.toggle(true)
                }
                is LoadingData.Success<CurrentChartInfo> -> {
                    with(loadingData.data) {
                        setChart(period, chartInfo)
                        setChanges(chartInfo.changes, chartInfo.changesColor)
                    }
                    chartProgressView.toggle(false)
                }
                is LoadingData.Error<CurrentChartInfo> -> {
                    loadingData.data?.let { data ->
                        setChart(data.period, data.chartInfo)
                    }
                    changesView.setCompoundDrawables(null, null, null, null)
                    changesView.text = ""
                    chartProgressView.toggle(false)
                }
            }
        }
        viewModel.transactionListLiveData.observe(viewLifecycleOwner) {
            adapter.submitList(it) {
                binding.listView.smoothScrollToPosition(0)
            }
        }
        viewModel.loadingData.listen({})
        viewModel.detailsLiveData.observe(viewLifecycleOwner) {
            //important download fee
            priceUsdView.text = getString(R.string.text_usd, it.priceUsd.toStringUsd())
            balanceCryptoView.text =
                getString(R.string.text_text, it.balance.toStringCoin(), viewModel.coinCode)
            balanceUsdView.text =
                getString(R.string.text_usd, (it.balance * it.priceUsd).toStringUsd())
            reservedCryptoView.text = getString(
                R.string.text_text,
                it.reservedBalanceCoin.toStringCoin(),
                it.reservedCode
            )
            reservedUsdView.text = getString(
                R.string.text_usd,
                it.reservedBalanceUsd.toStringUsd()
            )
        }
    }

    private fun initChart() {
        with(binding.chartView) {
            isDragEnabled = false
            isAutoScaleMinMaxEnabled = true
            isHighlightPerTapEnabled = false
            xAxis.isEnabled = false
            axisLeft.isEnabled = false
            axisRight.isEnabled = false
            legend.isEnabled = false
            description.isEnabled = false
            extraRightOffset = CHART_MARGIN_END_DP * resources.displayMetrics.density
            minOffset = 0f
            setScaleEnabled(false)
        }
    }

    private fun setChanges(changes: Double, @ChartChangesColor color: Int) {
        when (color) {
            ChartChangesColor.RED -> {
                binding.changesView.setDrawableStart(R.drawable.ic_arrow_drop_down)
                TextViewCompat.setCompoundDrawableTintList(
                    binding.changesView,
                    ContextCompat.getColorStateList(binding.changesView.context, R.color.chart_changes_down)
                )
                binding.changesView.setTextColor(
                    ContextCompat.getColor(
                        binding.changesView.context,
                        R.color.chart_changes_down
                    )
                )
            }
            ChartChangesColor.GREEN -> {
                binding.changesView.setDrawableStart(R.drawable.ic_arrow_drop_up)
                TextViewCompat.setCompoundDrawableTintList(
                    binding.changesView,
                    ContextCompat.getColorStateList(binding.changesView.context, R.color.chart_changes_up)
                )
                binding.changesView.setTextColor(
                    ContextCompat.getColor(
                        binding.changesView.context,
                        R.color.chart_changes_up
                    )
                )
            }
            ChartChangesColor.BLACK -> {
                binding.changesView.setCompoundDrawables(null, null, null, null)
                TextViewCompat.setCompoundDrawableTintList(
                    binding.changesView,
                    ContextCompat.getColorStateList(binding.changesView.context, R.color.chart_no_highlight)
                )
                binding.changesView.setTextColor(
                    ContextCompat.getColor(
                        binding.changesView.context,
                        R.color.chart_no_highlight
                    )
                )
            }
        }
        binding.changesView.text =
            resources.getString(
                R.string.transaction_changes_percent,
                String.format("%.2f", changes)
            )
    }

    private fun FragmentTransactionsBinding.setChart(@PriceChartPeriod chartType: Int, chartInfo: ChartDataItem) {
        when (chartType) {
            PriceChartPeriod.PERIOD_DAY -> chartChipGroupView.check(R.id.one_day_chip_view)
            PriceChartPeriod.PERIOD_WEEK -> chartChipGroupView.check(R.id.one_week_chip_view)
            PriceChartPeriod.PERIOD_MONTH -> chartChipGroupView.check(R.id.one_month_chip_view)
            PriceChartPeriod.PERIOD_QUARTER -> chartChipGroupView.check(R.id.three_month_chip_view)
            PriceChartPeriod.PERIOD_YEAR -> chartChipGroupView.check(R.id.one_year_chip_view)
        }
        val dataSet: LineDataSet
        val circleDataSet: LineDataSet
        if (chartView.data != null && chartView.data.dataSetCount > 0) {
            dataSet = chartView.data.getDataSetByIndex(0) as LineDataSet
            dataSet.values = chartInfo.prices
            circleDataSet = chartView.data.getDataSetByIndex(1) as LineDataSet
            circleDataSet.values = chartInfo.circles
            chartView.data.notifyDataChanged()
            chartView.notifyDataSetChanged()
        } else {
            dataSet = LineDataSet(chartInfo.prices, null).apply {
                mode = LineDataSet.Mode.CUBIC_BEZIER
                color = ContextCompat.getColor(chartView.context, R.color.chart_line)
                setDrawCircleHole(false)
                setDrawCircles(false)
                circleRadius = 3f
                setDrawValues(false)
            }
            circleDataSet = LineDataSet(chartInfo.circles, null).apply {
                mode = LineDataSet.Mode.CUBIC_BEZIER
                color = ContextCompat.getColor(chartView.context, R.color.chart_line)
                setCircleColor(ContextCompat.getColor(chartView.context, R.color.chart_point))
                setDrawCircleHole(false)
                circleRadius = 3f
                setDrawValues(false)
            }
            chartView.data = LineData(dataSet, circleDataSet)
        }
        chartView.invalidate()
    }

    companion object {
        private const val CHART_MARGIN_END_DP = 15F
    }
}