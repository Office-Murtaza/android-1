package com.app.belcobtm.presentation.features.wallet.transactions

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import com.app.belcobtm.R
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.presentation.core.extensions.*
import com.app.belcobtm.presentation.core.helper.AlertHelper
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.ui.BaseActivity
import com.app.belcobtm.presentation.features.authorization.pin.PinActivity
import com.app.belcobtm.presentation.features.wallet.deposit.DepositActivity
import com.app.belcobtm.presentation.features.wallet.exchange.coin.to.coin.ExchangeCoinToCoinActivity
import com.app.belcobtm.presentation.features.wallet.trade.main.TradeActivity
import com.app.belcobtm.presentation.features.wallet.transactions.adapter.TransactionsAdapter
import com.app.belcobtm.ui.main.coins.details.DetailsActivity
import com.app.belcobtm.ui.main.coins.sell.SellActivity
import com.app.belcobtm.ui.main.coins.send_gift.SendGiftActivity
import com.app.belcobtm.ui.main.coins.withdraw.WithdrawActivity
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.android.synthetic.main.activity_transactions.*
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnNeverAskAgain
import permissions.dispatcher.OnPermissionDenied
import permissions.dispatcher.RuntimePermissions

@RuntimePermissions
class TransactionsActivity : BaseActivity() {
    private val viewModel: TransactionsViewModel by viewModel { parametersOf(intent.getStringExtra(KEY_COIN_CODE)) }
    private val adapter: TransactionsAdapter = TransactionsAdapter(
        itemClickListener = {
            val intent = Intent(this, DetailsActivity::class.java)
            intent.putExtra(DetailsActivity.TAG_TRANSACTION_DETAILS_COIN_CODE, viewModel.coinCode)
            intent.putExtra(DetailsActivity.TAG_TRANSACTION_DETAILS_ID, it.id)
            intent.putExtra(DetailsActivity.TAG_TRANSACTION_DETAILS_DB_ID, it.dbId)
            startActivity(intent)
        },
        endListListener = { viewModel.updateTransactionList() }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transactions)
        initListeners()
        initObservers()
        initViews()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // NOTE: delegate the permission handling to generated method
        onRequestPermissionsResult(requestCode, grantResults)
    }

    @SuppressLint("MissingPermission")
    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun tradeOpen() {
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val location: Location? = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        val latitude: Double = location?.latitude ?: 0.0
        val longitude: Double = location?.longitude ?: 0.0
        showTradeScreen(latitude, longitude)
    }

    @OnNeverAskAgain(Manifest.permission.ACCESS_FINE_LOCATION)
    fun tradeNeverAskAgain() = showTradeScreen()

    @OnPermissionDenied(Manifest.permission.ACCESS_FINE_LOCATION)
    fun tradeDenied() = showTradeScreen()

    private fun initListeners() {
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
                WithdrawActivity.start(this, viewModel.coinDataItem)
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
                SendGiftActivity.start(this, viewModel.coinDataItem)
            } else {
                AlertHelper.showToastShort(
                    sendGiftButtonView.context,
                    "In progress. Only BTC, BCH, XRP, BNB and LTC withdraw available"
                )
            }
            fabMenuView.close(false)
        }

        sellButtonView.setOnClickListener {
            if (isCorrectCoinId()) {
                SellActivity.start(this, viewModel.coinDataItem)
            } else {
                AlertHelper.showToastShort(
                    sellButtonView.context,
                    "In progress. Only BTC, BCH, XRP, BNB and LTC withdraw available"
                )
            }
            fabMenuView.close(false)
        }
        c2cExchangeButtonView.setOnClickListener {
            if (isCorrectCoinId()) {
                val intent = Intent(this, ExchangeCoinToCoinActivity::class.java)
                intent.putExtra(ExchangeCoinToCoinActivity.TAG_COIN_ITEM, viewModel.coinDataItem)
                intent.putParcelableArrayListExtra(
                    ExchangeCoinToCoinActivity.TAG_COIN_ITEM_LIST,
                    viewModel.coinDataItemList
                )
                startActivity(intent)
            } else {
                AlertHelper.showToastShort(
                    c2cExchangeButtonView.context,
                    "In progress. Only BTC, BCH, XRP, BNB and LTC withdraw available"
                )
            }
            fabMenuView.close(false)
        }

        tradeButtonView.setOnClickListener { tradeOpenWithPermissionCheck() }

        fabMenuView.setOnMenuToggleListener {
            fabMenuView.isClickable = it
            if (it) {
                fabMenuView.setOnClickListener { fabMenuView.close(true) }
            } else {
                fabMenuView.setOnClickListener(null)
                fabMenuView.isClickable = false
                fabMenuView.isFocusable = false
            }
        }
    }


    private fun initObservers() {
        viewModel.chartLiveData.observe(this, Observer { loadingData ->
            when (loadingData) {
                is LoadingData.Loading -> progressView.show()
                is LoadingData.Success -> with(loadingData.data) {
                    updateChartByPeriod()
                    priceUsdView.text = getString(R.string.transaction_price_usd, priceUsd.toStringUsd())
                    balanceCryptoView.text =
                        getString(R.string.transaction_crypto_balance, balance.toStringCoin(), viewModel.coinCode)
                    balanceUsdView.text = getString(R.string.transaction_price_usd, (balance * priceUsd).toStringUsd())
                    progressView.hide()
                }
                is LoadingData.Error -> {
                    when (loadingData.errorType) {
                        is Failure.TokenError -> startActivity(Intent(this, PinActivity::class.java))
                        is Failure.MessageError -> showError(loadingData.errorType.message)
                        is Failure.NetworkConnection -> showError(R.string.error_internet_unavailable)
                        else -> showError(R.string.error_something_went_wrong)
                    }
                    progressView.hide()
                }
            }
        })
        viewModel.transactionListLiveData.observe(this, Observer {
            adapter.setItemList(it)
            swipeToRefreshView.isRefreshing = false
        })
    }

    private fun initViews() {
        setSupportActionBar(toolbarView)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = LocalCoinType.valueOf(viewModel.coinCode).fullName

        val dividerItemDecoration = DividerItemDecoration(listView.context, DividerItemDecoration.VERTICAL)
        ContextCompat.getDrawable(listView.context, R.drawable.divider_transactions)?.let {
            dividerItemDecoration.setDrawable(it)
        }
        listView.addItemDecoration(dividerItemDecoration)
        listView.adapter = adapter
        initChart()
    }

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
        viewModel.coinDataItem?.let {
            val intent = Intent(this, DepositActivity::class.java)
            intent.putExtra(DepositActivity.TAG_COIN_ITEM, it)
            startActivity(intent)
        }
    }

    private fun showTradeScreen(latitude: Double = 0.0, longitude: Double = 0.0) {
        if (isCorrectCoinId()) {
            val intent = Intent(this, TradeActivity::class.java)
            intent.putExtra(TradeActivity.TAG_COIN_ITEM, viewModel.coinDataItem)
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
        const val KEY_COIN_CODE = "key_coin_code"
        private const val CHART_MARGIN_END_DP = 15F
    }
}