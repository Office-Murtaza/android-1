package com.app.belcobtm.ui.main.coins.transactions

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.app.belcobtm.R
import com.app.belcobtm.api.model.response.CoinModel
import com.app.belcobtm.mvp.BaseMvpActivity
import com.app.belcobtm.ui.main.coins.sell.SellActivity
import com.app.belcobtm.ui.main.coins.send_gift.SendGiftActivity
import com.app.belcobtm.ui.main.coins.withdraw.WithdrawActivity
import com.app.belcobtm.presentation.core.QRUtils.Companion.getSpacelessQR
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_show_phone.container
import kotlinx.android.synthetic.main.activity_show_phone.toolbar
import kotlinx.android.synthetic.main.activity_transactions.*
import org.parceler.Parcels

class TransactionsActivity :
    BaseMvpActivity<TransactionsContract.View, TransactionsContract.Presenter>(),
    TransactionsContract.View {

    companion object {
        private const val KEY_COIN = "KEY_COIN"

        @JvmStatic
        fun start(context: Context?, coin: CoinModel) {
            val intent = Intent(context, TransactionsActivity::class.java)
            intent.putExtra(KEY_COIN, Parcels.wrap(coin))
            context?.startActivity(intent)
        }
    }

    private lateinit var mCoin: CoinModel
    private lateinit var mAdapter: TransactionsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transactions)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        mCoin = Parcels.unwrap(intent.getParcelableExtra(KEY_COIN))

        initView()
        mPresenter.coinId = mCoin.coinId

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

    override fun onStart() {
        super.onStart()
        showProgress(true)
        mPresenter.getFirstTransactions()
    }


    private fun initView() {
        supportActionBar?.title = mCoin.fullCoinName

        mAdapter = TransactionsAdapter(mPresenter.transactionList, mCoin) {
            mPresenter.getTransactions()
        }
        transaction_recycler.adapter = mAdapter

        swipe_refresh.setColorSchemeColors(
            Color.RED, Color.GREEN, Color.BLUE
        )

        price_usd.text = "${mCoin.price.uSD} USD"

        val balance = if (mCoin.balance > 0)
            String.format("%.6f", mCoin.balance).trimEnd('0')
        else "0"

        balance_crypto.text = "$balance ${mCoin.coinId}"

        val amountUsd = mCoin.balance * mCoin.price.uSD
        balance_usd.text = "${String.format("%.2f", amountUsd)} USD"
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
                Snackbar.make(container, R.string.wallet_code_clipboard, Snackbar.LENGTH_LONG)
                    .show()
            }
            .create()
        dialog.show()

        dialog.findViewById<AppCompatTextView>(R.id.wallet_code)?.text = mCoin.publicKey

        /*val walletQrCode =
            BarcodeEncoder().encodeBitmap(mCoin.publicKey,
                BarcodeFormat.QR_CODE, 200, 200)

        dialog.findViewById<AppCompatImageView>(R.id.wallet_qr_code)?.setImageBitmap(walletQrCode)*/

        dialog.findViewById<AppCompatImageView>(R.id.wallet_qr_code)?.setImageBitmap(getSpacelessQR(mCoin.publicKey,200,200))

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
}
