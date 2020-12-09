package com.app.belcobtm.presentation.features.wallet.transactions

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.view.Menu
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DividerItemDecoration
import com.app.belcobtm.R
import com.app.belcobtm.data.rest.wallet.request.PriceChartPeriod
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.domain.wallet.item.ChartChangesColor
import com.app.belcobtm.domain.wallet.item.ChartDataItem
import com.app.belcobtm.presentation.core.extensions.setDrawableStart
import com.app.belcobtm.presentation.core.extensions.toStringCoin
import com.app.belcobtm.presentation.core.extensions.toStringUsd
import com.app.belcobtm.presentation.core.extensions.toggle
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.app.belcobtm.presentation.features.wallet.trade.main.TradeActivity
import com.app.belcobtm.presentation.features.wallet.transactions.TransactionsFABType.*
import com.app.belcobtm.presentation.features.wallet.transactions.adapter.TransactionsAdapter
import com.app.belcobtm.presentation.features.wallet.transactions.item.CurrentChartInfo
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import io.github.kobakei.materialfabspeeddial.FabSpeedDialMenu
import kotlinx.android.synthetic.main.fragment_transactions.*
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnNeverAskAgain
import permissions.dispatcher.OnPermissionDenied
import permissions.dispatcher.RuntimePermissions

@RuntimePermissions
class TransactionsFragment : BaseFragment() {
    private val viewModel: TransactionsViewModel by viewModel {
        parametersOf(TransactionsFragmentArgs.fromBundle(requireArguments()).coinCode)
    }

    private val adapter: TransactionsAdapter = TransactionsAdapter(
        itemClickListener = {
            val transactionId = if (it.id.isBlank()) it.dbId else it.id
            navigate(
                TransactionsFragmentDirections.toTransactionDetailsFragment(
                    viewModel.coinCode,
                    transactionId
                )
            )
        },
        endListListener = { viewModel.updateTransactionList() }
    )
    override val resourceLayout: Int = R.layout.fragment_transactions
    override val isToolbarEnabled: Boolean = true
    override val isHomeButtonEnabled: Boolean = true
    override var isMenuEnabled: Boolean = true
    override val customToolbarId: Int = R.id.customToolbarView
    override val isFirstShowContent: Boolean = false
    override val retryListener: View.OnClickListener = View.OnClickListener {
        viewModel.updateData()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // NOTE: delegate the permission handling to generated method
        onRequestPermissionsResult(requestCode, grantResults)
    }

    private fun initFabMenu() {
        val menu = FabSpeedDialMenu(requireContext())
        if (viewModel.coinCode == LocalCoinType.CATM.name) {
            addButtonToMenu(menu, STAKING)
        }
//        addButtonToMenu(groupId, menu, TRADE)
//        addButtonToMenu(groupId, menu, SELL)
        addButtonToMenu(menu, SEND_GIFT)
        addButtonToMenu(menu, WITHDRAW)
        addButtonToMenu(menu, DEPOSIT)
        addButtonToMenu(menu, RECALL)
        addButtonToMenu(menu, RESERVE)
        fabListView.setMenu(menu)
    }

    private fun addButtonToMenu(menu: Menu, buttonType: TransactionsFABType) {
        menu
            .add(Menu.FIRST, buttonType.id, buttonType.ordinal, buttonType.resText)
            .setIcon(buttonType.resIcon)
    }

    override fun initViews() {
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

    override fun initListeners() {
        chartChipGroupView.setOnCheckedChangeListener { _, checkedId ->
            viewModel.changeCurrentTypePeriod(checkedId)
        }
        swipeToRefreshView.setOnRefreshListener { viewModel.refreshTransactionList() }
        fabListView.addOnMenuItemClickListener { _, _, itemId ->
            when (itemId) {
                TRADE.id -> tradeOpenWithPermissionCheck()
                STAKING.id -> navigate(TransactionsFragmentDirections.toStakingFragment())
                SEND_GIFT.id ->
                    navigate(TransactionsFragmentDirections.toSendGiftFragment(viewModel.coinCode))
                WITHDRAW.id ->
                    navigate(TransactionsFragmentDirections.toWithdrawFragment(viewModel.coinCode))
                DEPOSIT.id ->
                    navigate(TransactionsFragmentDirections.toDepositFragment(viewModel.coinCode))
                RECALL.id ->
                    navigate(TransactionsFragmentDirections.toRecallFragment(viewModel.coinCode))
                RESERVE.id ->
                    navigate(TransactionsFragmentDirections.toReserveFragment(viewModel.coinCode))

                //SELL.id -> SellActivity.start(
                //    requireContext(),
                //    viewModel.coinDataItem,
                //    viewModel.coinDataItemList
                //)
            }
        }
    }

    override fun initObservers() {
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
        viewModel.transactionListLiveData.observe(this) {
            adapter.submitList(it)
            swipeToRefreshView.isRefreshing = false
        }
        viewModel.detailsLiveData.listen({
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
        })
    }

    @SuppressLint("MissingPermission")
    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun tradeOpen() {
        val lm = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val location: Location? = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        val latitude: Double = location?.latitude ?: 0.0
        val longitude: Double = location?.longitude ?: 0.0
        showTradeScreen(latitude, longitude)
    }

    @OnNeverAskAgain(Manifest.permission.ACCESS_FINE_LOCATION)
    fun tradeNeverAskAgain() = showTradeScreen()

    @OnPermissionDenied(Manifest.permission.ACCESS_FINE_LOCATION)
    fun tradeDenied() = showTradeScreen()

    private fun initChart() {
        with(chartView) {
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
                changesView.setDrawableStart(R.drawable.ic_arrow_drop_down)
                changesView.compoundDrawableTintList =
                    ContextCompat.getColorStateList(changesView.context, R.color.chart_changes_down)
                changesView.setTextColor(
                    ContextCompat.getColor(
                        changesView.context,
                        R.color.chart_changes_down
                    )
                )
            }
            ChartChangesColor.GREEN -> {
                changesView.setDrawableStart(R.drawable.ic_arrow_drop_up)
                changesView.compoundDrawableTintList =
                    ContextCompat.getColorStateList(changesView.context, R.color.chart_changes_up)
                changesView.setTextColor(
                    ContextCompat.getColor(
                        changesView.context,
                        R.color.chart_changes_up
                    )
                )
            }
            ChartChangesColor.BLACK -> {
                changesView.setCompoundDrawables(null, null, null, null)
                changesView.compoundDrawableTintList =
                    ContextCompat.getColorStateList(changesView.context, R.color.chart_no_highlight)
                changesView.setTextColor(
                    ContextCompat.getColor(
                        changesView.context,
                        R.color.chart_no_highlight
                    )
                )
            }
        }
        changesView.text =
            resources.getString(
                R.string.transaction_changes_percent,
                String.format("%.2f", changes)
            )
    }

    private fun setChart(@PriceChartPeriod chartType: Int, chartInfo: ChartDataItem) {
        when (chartType) {
            PriceChartPeriod.PERIOD_DAY -> chartChipGroupView.check(R.id.oneDayChipView)
            PriceChartPeriod.PERIOD_WEEK -> chartChipGroupView.check(R.id.oneWeekChipView)
            PriceChartPeriod.PERIOD_MONTH -> chartChipGroupView.check(R.id.oneMonthChipView)
            PriceChartPeriod.PERIOD_QUARTER -> chartChipGroupView.check(R.id.threeMonthChipView)
            PriceChartPeriod.PERIOD_YEAR -> chartChipGroupView.check(R.id.oneYearChipView)
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

    private fun showTradeScreen(latitude: Double = 0.0, longitude: Double = 0.0) {
        val intent = Intent(requireContext(), TradeActivity::class.java)
        intent.putExtra(
            TradeActivity.TAG_COIN_CODE,
            TransactionsFragmentArgs.fromBundle(requireArguments()).coinCode
        )
        intent.putExtra(TradeActivity.TAG_LATITUDE, latitude)
        intent.putExtra(TradeActivity.TAG_LONGITUDE, longitude)
        startActivity(intent)
    }

    companion object {
        private const val CHART_MARGIN_END_DP = 15F
    }
}