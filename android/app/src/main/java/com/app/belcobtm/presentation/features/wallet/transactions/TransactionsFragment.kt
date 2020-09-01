package com.app.belcobtm.presentation.features.wallet.transactions

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import com.app.belcobtm.R
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.presentation.core.extensions.setDrawableStart
import com.app.belcobtm.presentation.core.extensions.toStringCoin
import com.app.belcobtm.presentation.core.extensions.toStringUsd
import com.app.belcobtm.presentation.core.helper.AlertHelper
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.app.belcobtm.presentation.features.wallet.trade.main.TradeActivity
import com.app.belcobtm.presentation.features.wallet.transactions.adapter.TransactionsAdapter
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
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
            navigate(TransactionsFragmentDirections.toTransactionDetailsFragment(viewModel.coinCode, transactionId))
        },
        endListListener = { viewModel.updateTransactionList() }
    )
    override val resourceLayout: Int = R.layout.fragment_transactions
    override val isToolbarEnabled: Boolean = true
    override val isHomeButtonEnabled: Boolean = true
    override var isMenuEnabled: Boolean = false
    override val customToolbarId: Int = R.id.customToolbarView
    override val isFirstShowContent: Boolean = false
    override val retryListener: View.OnClickListener = View.OnClickListener { viewModel.updateData() }

    override fun onResume() {
        super.onResume()
        viewModel.updateData()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // NOTE: delegate the permission handling to generated method
        onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun initViews() {
        setToolbarTitle(LocalCoinType.valueOf(viewModel.coinCode).fullName)
        ContextCompat.getDrawable(listView.context, R.drawable.bg_divider)?.let {
            val dividerItemDecoration = DividerItemDecoration(listView.context, DividerItemDecoration.VERTICAL)
            dividerItemDecoration.setDrawable(it)
            listView.addItemDecoration(dividerItemDecoration)
        }
        listView.adapter = adapter
        initChart()
//        if (viewModel.coinCode == LocalCoinType.CATM.name) {
//            stakingButtonView.show()
//        } else {
//            stakingButtonView.hide()
//        }
    }

    override fun initListeners() {
        chartChipGroupView.setOnCheckedChangeListener { _, checkedId ->
            viewModel.currentChartPeriodType = when (checkedId) {
                R.id.oneWeekChipView -> ChartPeriodType.WEEK
                R.id.oneMonthChipView -> ChartPeriodType.MONTH
                R.id.threeMonthChipView -> ChartPeriodType.THREE_MONTHS
                R.id.oneYearChipView -> ChartPeriodType.YEAR
                else -> ChartPeriodType.DAY
            }
            updateChartByPeriod()
        }
        swipeToRefreshView.setOnRefreshListener { viewModel.refreshTransactionList() }
        depositButtonView.setOnClickListener {
            showDepositDialog()
            fabMenuView.close(true)
        }
        withdrawButtonView.setOnClickListener {
            if (isCorrectCoinId()) {
                navigate(TransactionsFragmentDirections.toWithdrawFragment(viewModel.coinDataItem?.code ?: ""))
            } else {
                AlertHelper.showToastShort(
                    withdrawButtonView.context,
                    "In progress. Only BTC, BCH, XRP, BNB and LTC withdraw available"
                )
            }
            fabMenuView.close(false)
        }
        sendGiftButtonView.setOnClickListener {
            if (isCorrectCoinId()) {
                navigate(TransactionsFragmentDirections.toSendGiftFragment(viewModel.coinDataItem?.code ?: ""))
            } else {
                AlertHelper.showToastShort(
                    sendGiftButtonView.context,
                    "In progress. Only BTC, BCH, XRP, BNB and LTC withdraw available"
                )
            }
            fabMenuView.close(false)
        }

//        sellButtonView.setOnClickListener {
//            if (isCorrectCoinId()) {
//                SellActivity.start(requireContext(), viewModel.coinDataItem, viewModel.coinDataItemList)
//            } else {
//                AlertHelper.showToastShort(
//                    sellButtonView.context,
//                    "In progress. Only BTC, BCH, XRP, BNB and LTC withdraw available"
//                )
//            }
//            fabMenuView.close(false)
//        }
//        c2cExchangeButtonView.setOnClickListener {
//            if (isCorrectCoinId()) {
//                navigate(TransactionsFragmentDirections.toExchangeFragment(viewModel.coinDataItem?.code ?: ""))
//            } else {
//                AlertHelper.showToastShort(
//                    c2cExchangeButtonView.context,
//                    "In progress. Only BTC, BCH, XRP, BNB and LTC withdraw available"
//                )
//            }
//            fabMenuView.close(false)
//        }
//
//        tradeButtonView.setOnClickListener { tradeOpenWithPermissionCheck() }
//
//        stakingButtonView.setOnClickListener {
//            if (isCorrectCoinId()) {
//                startActivity(Intent(requireContext(), StakingActivity::class.java))
//            } else {
//                AlertHelper.showToastShort(
//                    c2cExchangeButtonView.context,
//                    "In progress. Only BTC, BCH, XRP, BNB and LTC withdraw available"
//                )
//            }
//            fabMenuView.close(false)
//        }

        fabMenuView.setOnMenuToggleListener {
            fabMenuView?.isClickable = it
            if (it) {
                fabMenuView?.setOnClickListener { fabMenuView?.close(true) }
            } else {
                fabMenuView?.setOnClickListener(null)
                fabMenuView?.isClickable = false
                fabMenuView?.isFocusable = false
            }
        }
    }

    override fun initObservers() {
        viewModel.chartLiveData.listen({
            updateChartByPeriod()
            priceUsdView.text = getString(R.string.unit_usd_dynamic_symbol, it.priceUsd.toStringUsd())
            balanceCryptoView.text =
                getString(R.string.transaction_crypto_balance, it.balance.toStringCoin(), viewModel.coinCode)
            balanceUsdView.text = getString(R.string.unit_usd_dynamic_symbol, (it.balance * it.priceUsd).toStringUsd())
        })
        viewModel.transactionListLiveData.observe(this, {
            adapter.setItemList(it)
            swipeToRefreshView.isRefreshing = false
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

    private fun updateChartByPeriod() {
        val loadingData = viewModel.chartLiveData.value
        if (loadingData is LoadingData.Success) {
            with(loadingData.data) {
                when (val chartType = viewModel.currentChartPeriodType) {
                    ChartPeriodType.DAY -> {
                        setChart(chartType, chartDay.second)
                        setChanges(chartDay.first)
                    }
                    ChartPeriodType.WEEK -> {
                        setChart(chartType, chartWeek.second)
                        setChanges(chartWeek.first)
                    }
                    ChartPeriodType.MONTH -> {
                        setChart(chartType, chartMonth.second)
                        setChanges(chartMonth.first)
                    }
                    ChartPeriodType.THREE_MONTHS -> {
                        setChart(chartType, chartThreeMonths.second)
                        setChanges(chartThreeMonths.first)
                    }
                    ChartPeriodType.YEAR -> {
                        setChart(chartType, chartYear.second)
                        setChanges(chartYear.first)
                    }
                }
            }
        }
    }

    private fun setChanges(changes: Double) {
        if (changes >= 0) {
            changesView.setDrawableStart(R.drawable.ic_arrow_drop_up)
            changesView.compoundDrawableTintList =
                ContextCompat.getColorStateList(changesView.context, R.color.chart_changes_up)
            changesView.setTextColor(ContextCompat.getColor(changesView.context, R.color.chart_changes_up))
        } else {
            changesView.setDrawableStart(R.drawable.ic_arrow_drop_down)
            changesView.compoundDrawableTintList =
                ContextCompat.getColorStateList(changesView.context, R.color.chart_changes_down)
            changesView.setTextColor(ContextCompat.getColor(changesView.context, R.color.chart_changes_down))
        }

        changesView.text = resources.getString(R.string.transaction_changes_percent, changes.toString())
    }

    private fun setChart(chartType: ChartPeriodType, chartList: List<Double>) {
        when (chartType) {
            ChartPeriodType.DAY -> chartChipGroupView.check(R.id.oneDayChipView)
            ChartPeriodType.WEEK -> chartChipGroupView.check(R.id.oneWeekChipView)
            ChartPeriodType.MONTH -> chartChipGroupView.check(R.id.oneMonthChipView)
            ChartPeriodType.THREE_MONTHS -> chartChipGroupView.check(R.id.threeMonthChipView)
            ChartPeriodType.YEAR -> chartChipGroupView.check(R.id.oneYearChipView)
        }

        val valueList = chartList.mapIndexed { index, value -> BarEntry(index.toFloat(), value.toFloat()) }
        val dataSet = LineDataSet(valueList, null).apply {
            mode = LineDataSet.Mode.CUBIC_BEZIER
            color = ContextCompat.getColor(chartView.context, R.color.chart_line)
            setCircleColors(
                chartList.mapIndexed { index, _ ->
                    if (index == chartList.lastIndex) R.color.chart_point else android.R.color.transparent
                }.toIntArray(),
                chartView.context
            )
            setDrawCircleHole(false)
            circleRadius = 3f
            setDrawValues(false)
        }

        chartView.data = LineData(dataSet)
        chartView.invalidate()
    }

    private fun showDepositDialog() {
        navigate(
            TransactionsFragmentDirections.toDepositFragment(
                viewModel.coinDataItem?.code ?: "",
                viewModel.coinDataItem?.publicKey ?: ""
            )
        )
    }

    private fun showTradeScreen(latitude: Double = 0.0, longitude: Double = 0.0) {
        if (isCorrectCoinId()) {
            val intent = Intent(requireContext(), TradeActivity::class.java)
            intent.putExtra(
                TradeActivity.TAG_COIN_CODE,
                TransactionsFragmentArgs.fromBundle(requireArguments()).coinCode
            )
            intent.putExtra(TradeActivity.TAG_LATITUDE, latitude)
            intent.putExtra(TradeActivity.TAG_LONGITUDE, longitude)
            startActivity(intent)
        } else {
            AlertHelper.showToastShort(
                fabMenuView.context,
                "In progress. Only BTC, BCH, XRP, ETH, BNB and LTC withdraw available"
            )
        }
        fabMenuView.close(false)
    }

    private fun isCorrectCoinId(): Boolean = viewModel.coinCode == LocalCoinType.BTC.name
            || viewModel.coinCode == LocalCoinType.BCH.name
            || viewModel.coinCode == LocalCoinType.ETH.name
            || viewModel.coinCode == LocalCoinType.LTC.name
            || viewModel.coinCode == LocalCoinType.XRP.name
            || viewModel.coinCode == LocalCoinType.TRX.name
            || viewModel.coinCode == LocalCoinType.BNB.name
            || viewModel.coinCode == LocalCoinType.CATM.name

    companion object {
        private const val CHART_MARGIN_END_DP = 15F
    }
}