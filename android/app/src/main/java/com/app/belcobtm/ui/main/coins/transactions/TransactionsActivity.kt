package com.app.belcobtm.ui.main.coins.transactions

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import com.app.belcobtm.R
import com.app.belcobtm.api.model.response.CoinModel
import com.app.belcobtm.mvp.BaseMvpActivity
import com.app.belcobtm.presentation.core.QRUtils.Companion.getSpacelessQR
import com.app.belcobtm.presentation.core.extensions.setDrawableStart
import com.app.belcobtm.presentation.core.helper.AlertHelper
import com.app.belcobtm.ui.main.coins.sell.SellActivity
import com.app.belcobtm.ui.main.coins.send_gift.SendGiftActivity
import com.app.belcobtm.ui.main.coins.withdraw.WithdrawActivity
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.android.synthetic.main.activity_show_phone.container
import kotlinx.android.synthetic.main.activity_show_phone.toolbar
import kotlinx.android.synthetic.main.activity_transactions.*
import org.parceler.Parcels

class TransactionsActivity : BaseMvpActivity<TransactionsContract.View, TransactionsContract.Presenter>(),
    TransactionsContract.View {
    private lateinit var mCoin: CoinModel
    private lateinit var mAdapter: TransactionsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transactions)
        mCoin = Parcels.unwrap(intent.getParcelableExtra(KEY_COIN))
        mPresenter.coinId = mCoin.coinId
        initViews()
        initListeners()
    }

    override fun onStart() {
        super.onStart()
        showProgress(true)
        mPresenter.getFirstTransactions()
    }

    override fun setPrice(price: Double) {
        val convertedPrice = if (price > 0) String.format("%.2f", price).trimEnd('0') else "0"
        priceUsdView.text = getString(R.string.transaction_price_usd, convertedPrice)
    }

    override fun setBalance(balance: Double) {
        val convertedBalance = if (balance > 0) String.format("%.6f", balance).trimEnd('0') else "0"
        balanceCryptoView.text = getString(R.string.transaction_crypto_balance, convertedBalance, mCoin.coinId)
    }

    override fun setChanges(changes: Double) {
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

    override fun setChart(chartType: ChartPeriodType, chartList: List<Double>) {
        when (chartType) {
            ChartPeriodType.DAY -> oneDayButtonView.isChecked = true
            ChartPeriodType.WEEK -> oneWeekButtonView.isChecked = true
            ChartPeriodType.MONTH -> oneMonthButtonView.isChecked = true
            ChartPeriodType.THREE_MONTHS -> threeMonthsButtonView.isChecked = true
            ChartPeriodType.YEAR -> oneYearButtonView.isChecked = true
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

    private fun initViews() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = mCoin.fullCoinName

        mAdapter = TransactionsAdapter(mPresenter.transactionList, mCoin) {
            mPresenter.getTransactions()
        }
        recyclerView.adapter = mAdapter

        val dividerItemDecoration = DividerItemDecoration(recyclerView.context, DividerItemDecoration.VERTICAL)
        ContextCompat.getDrawable(recyclerView.context, R.drawable.divider_transactions)?.let {
            dividerItemDecoration.setDrawable(it)
        }
        recyclerView.addItemDecoration(dividerItemDecoration)


        swipe_refresh.setColorSchemeColors(
            Color.RED, Color.GREEN, Color.BLUE
        )

        val amountUsd = mCoin.balance * mCoin.price.uSD
        balanceUsdView.text = "${String.format("%.2f", amountUsd)} USD"

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
            extraRightOffset = dpToPx(CHART_MARGIN_END_DP)
            minOffset = 0f
            setScaleEnabled(false)
        }

        mPresenter.chartViewInitialized()
    }

    private fun initListeners() {
        oneDayButtonView.setOnClickListener { mPresenter.chartButtonClicked(ChartPeriodType.DAY) }
        oneWeekButtonView.setOnClickListener { mPresenter.chartButtonClicked(ChartPeriodType.WEEK) }
        oneMonthButtonView.setOnClickListener { mPresenter.chartButtonClicked(ChartPeriodType.MONTH) }
        threeMonthsButtonView.setOnClickListener { mPresenter.chartButtonClicked(ChartPeriodType.THREE_MONTHS) }
        oneYearButtonView.setOnClickListener { mPresenter.chartButtonClicked(ChartPeriodType.YEAR) }

        swipe_refresh.setOnRefreshListener { mPresenter.getFirstTransactions() }
        deposit.setOnClickListener { showDepositDialog() }
        withdraw.setOnClickListener {
            if (mCoin.coinId == "BTC"
                || mCoin.coinId == "BCH"
                || mCoin.coinId == "ETH"
                || mCoin.coinId == "LTC"
                || mCoin.coinId == "XRP"
                || mCoin.coinId == "TRX"
                || mCoin.coinId == "BNB"
            ) {
                WithdrawActivity.start(this, mCoin)
            } else {
                showMessage("In progress. Only BTC, BCH, XRP, BNB and LTC withdraw available")
            }
        }
        send_gift.setOnClickListener {
            if (mCoin.coinId == "BTC"
                || mCoin.coinId == "BCH"
                || mCoin.coinId == "ETH"
                || mCoin.coinId == "LTC"
                || mCoin.coinId == "XRP"
                || mCoin.coinId == "TRX"
                || mCoin.coinId == "BNB"
            ) {
                SendGiftActivity.start(this, mCoin)
            } else {
                showMessage("In progress. Only BTC, BCH, XRP, ETH, BNB and LTC withdraw available")
            }
        }

        sell.setOnClickListener {
            if (mCoin.coinId == "BTC"
                || mCoin.coinId == "BCH"
                || mCoin.coinId == "ETH"
                || mCoin.coinId == "LTC"
                || mCoin.coinId == "XRP"
                || mCoin.coinId == "TRX"
                || mCoin.coinId == "BNB"
            ) {
                SellActivity.start(this, mCoin)
            } else {
                showMessage("In progress. Only BTC, BCH, XRP, ETH, BNB and LTC withdraw available")
            }
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showDepositDialog() {

        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.deposit) + " " + mCoin.coinId)
            .setView(R.layout.dialog_deposit)
            .setPositiveButton(R.string.copy) { dialog, _ ->
                copyToClipboard(getString(R.string.wallet_code_clipboard), mCoin.publicKey)
                dialog.cancel()
                AlertHelper.showToastLong(container.context, R.string.wallet_code_clipboard)
            }
            .create()
        dialog.show()

        dialog.findViewById<AppCompatTextView>(R.id.wallet_code)?.text = mCoin.publicKey

        /*val walletQrCode =
            BarcodeEncoder().encodeBitmap(mCoin.publicKey,
                BarcodeFormat.QR_CODE, 200, 200)

        dialog.findViewById<AppCompatImageView>(R.id.wallet_qr_code)?.setImageBitmap(walletQrCode)*/

        dialog.findViewById<AppCompatImageView>(R.id.wallet_qr_code)
            ?.setImageBitmap(getSpacelessQR(mCoin.publicKey, 200, 200))

    }

    override fun notifyTransactions() {
        runOnUiThread {
            mAdapter.notifyDataSetChanged()
        }
    }

    override fun showProgress(show: Boolean) {
        runOnUiThread {
            if (!show)
                swipe_refresh.isRefreshing = false

            super.showProgress(show)
        }
    }

    fun dpToPx(dp: Float): Float {
        val density: Float = resources.displayMetrics.density
        return dp.toFloat() * density
    }

    companion object {
        private const val KEY_COIN = "KEY_COIN"
        private const val CHART_MARGIN_END_DP = 15F

        @JvmStatic
        fun start(context: Context?, coin: CoinModel) {
            val intent = Intent(context, TransactionsActivity::class.java)
            intent.putExtra(KEY_COIN, Parcels.wrap(coin))
            context?.startActivity(intent)
        }
    }
}
