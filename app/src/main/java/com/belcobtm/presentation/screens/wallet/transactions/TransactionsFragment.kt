package com.belcobtm.presentation.screens.wallet.transactions

import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.DividerItemDecoration
import com.belcobtm.R
import com.belcobtm.data.rest.wallet.request.PriceChartPeriod
import com.belcobtm.databinding.FragmentTransactionsBinding
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.domain.wallet.item.ChartChangesColor
import com.belcobtm.domain.wallet.item.ChartDataItem
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.belcobtm.presentation.screens.wallet.transactions.TransactionsFABType.DEPOSIT
import com.belcobtm.presentation.screens.wallet.transactions.TransactionsFABType.RECALL
import com.belcobtm.presentation.screens.wallet.transactions.TransactionsFABType.RESERVE
import com.belcobtm.presentation.screens.wallet.transactions.TransactionsFABType.STAKING
import com.belcobtm.presentation.screens.wallet.transactions.TransactionsFABType.WITHDRAW
import com.belcobtm.presentation.screens.wallet.transactions.adapter.TransactionsAdapter
import com.belcobtm.presentation.screens.wallet.transactions.item.CurrentChartInfo
import com.belcobtm.presentation.tools.extensions.hide
import com.belcobtm.presentation.tools.extensions.setDrawableStart
import com.belcobtm.presentation.tools.extensions.toStringCoin
import com.belcobtm.presentation.tools.extensions.toggle
import com.belcobtm.presentation.tools.formatter.CryptoPriceFormatter
import com.belcobtm.presentation.tools.formatter.CurrencyPriceFormatter
import com.belcobtm.presentation.tools.formatter.Formatter
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import io.github.kobakei.materialfabspeeddial.FabSpeedDialMenu
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named

class TransactionsFragment : BaseFragment<FragmentTransactionsBinding>() {

    private val viewModel: TransactionsViewModel by viewModel {
        parametersOf(TransactionsFragmentArgs.fromBundle(requireArguments()).coinCode)
    }

    private val adapter: TransactionsAdapter = TransactionsAdapter {
        val transactionId = it.id.ifBlank { it.dbId }
        navigate(
            TransactionsFragmentDirections.toTransactionDetailsFragment(
                viewModel.coinCode,
                transactionId
            )
        )
    }
    override val isBackButtonEnabled: Boolean = true
    override var isMenuEnabled: Boolean = true
    override val isFirstShowContent: Boolean = false
    override val retryListener: View.OnClickListener = View.OnClickListener {
        viewModel.updateData()
    }
    private val currencyFormatter: Formatter<Double> by inject(
        named(CurrencyPriceFormatter.CURRENCY_PRICE_FORMATTER_QUALIFIER)
    )
    private val cryptoPriceFormatter: Formatter<Double> by inject(
        named(CryptoPriceFormatter.CRYPTO_PRICE_FORMATTER_QUALIFIER)
    )

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentTransactionsBinding =
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
        menu.add(Menu.FIRST, buttonType.id, buttonType.ordinal, buttonType.resText)
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

    override fun FragmentTransactionsBinding.initListeners() {
        chartChipGroupView.setOnCheckedChangeListener { _, checkedId ->
            viewModel.changeCurrentTypePeriod(checkedId)
        }
        refreshTransactionsLayout.setOnRefreshListener {
            viewModel.fetchTransactions()
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
                else -> {
                }
            }
        }
        viewModel.transactionListLiveData.observe(viewLifecycleOwner) {
            if (it.isEmpty()) {
                refreshTransactionsLayout.isVisible = false
                noTransactionsTextView.isVisible = true
            } else {
                refreshTransactionsLayout.isVisible = true
                noTransactionsTextView.isVisible = false
                adapter.submitList(it) {
                    binding.listView.smoothScrollToPosition(0)
                }
            }
        }
        viewModel.loadingData.listen(success = {
            refreshTransactionsLayout.isRefreshing = false
        })
        viewModel.detailsLiveData.observe(viewLifecycleOwner) {
            //important download fee
            priceUsdView.text = cryptoPriceFormatter.format(it.priceUsd)
            balanceCryptoView.text =
                getString(R.string.text_text, it.balance.toStringCoin(), viewModel.coinCode)
            balanceUsdView.text = currencyFormatter.format(it.balance * it.priceUsd)
            reservedCryptoView.text = getString(
                R.string.text_text,
                it.reservedBalanceCoin.toStringCoin(),
                it.reservedCode
            )
            reservedUsdView.text = currencyFormatter.format(it.reservedBalanceUsd)
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
                    ContextCompat.getColorStateList(
                        binding.changesView.context,
                        R.color.mainRed
                    )
                )
                binding.changesView.setTextColor(
                    ContextCompat.getColor(
                        binding.changesView.context,
                        R.color.mainRed
                    )
                )
            }
            ChartChangesColor.GREEN -> {
                binding.changesView.setDrawableStart(R.drawable.ic_arrow_drop_up)
                TextViewCompat.setCompoundDrawableTintList(
                    binding.changesView,
                    ContextCompat.getColorStateList(
                        binding.changesView.context,
                        R.color.mainGreen
                    )
                )
                binding.changesView.setTextColor(
                    ContextCompat.getColor(
                        binding.changesView.context,
                        R.color.mainGreen
                    )
                )
            }
            ChartChangesColor.BLACK -> {
                binding.changesView.setCompoundDrawables(null, null, null, null)
                TextViewCompat.setCompoundDrawableTintList(
                    binding.changesView,
                    ContextCompat.getColorStateList(
                        binding.changesView.context,
                        R.color.chart_no_highlight
                    )
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
            ).replace("-", "")
    }

    private fun FragmentTransactionsBinding.setChart(
        chartType: PriceChartPeriod,
        chartInfo: ChartDataItem
    ) {
        when (chartType) {
            PriceChartPeriod.DAY -> chartChipGroupView.check(R.id.one_day_chip_view)
            PriceChartPeriod.WEEK -> chartChipGroupView.check(R.id.one_week_chip_view)
            PriceChartPeriod.MONTH -> chartChipGroupView.check(R.id.one_month_chip_view)
            PriceChartPeriod.MONTH_3 -> chartChipGroupView.check(R.id.three_month_chip_view)
            PriceChartPeriod.YEAR -> chartChipGroupView.check(R.id.one_year_chip_view)
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
