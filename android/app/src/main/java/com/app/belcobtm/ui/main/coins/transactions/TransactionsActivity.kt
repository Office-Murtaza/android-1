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
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.android.synthetic.main.activity_show_phone.container
import kotlinx.android.synthetic.main.activity_show_phone.toolbar
import kotlinx.android.synthetic.main.activity_transactions.*
import org.parceler.Parcels

class TransactionsActivity : BaseMvpActivity<TransactionsContract.View, TransactionsContract.Presenter>(),
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

        showProgress(true)
        mPresenter.getFirstTransactions()

        swipe_refresh.setOnRefreshListener { mPresenter.getFirstTransactions() }
        deposit.setOnClickListener { showDepositDialog() }
    }


    private fun initView() {
        supportActionBar?.title = mCoin.fullCoinName

        mAdapter = TransactionsAdapter(mPresenter.transactionList) {
            mPresenter.getTransactions()
        }
        transaction_recycler.adapter = mAdapter

        swipe_refresh.setColorSchemeColors(
            Color.RED, Color.GREEN, Color.BLUE
        )

        price_usd.text = "${mCoin.price.uSD} USD"
        balance_crypto.text = "${mCoin.balance} ${mCoin.coinId}"
        balance_usd.text = "${mCoin.balance * mCoin.price.uSD} USD"
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
            .setTitle(getString(R.string.deposit))
            .setView(R.layout.dialog_deposit)
            .setPositiveButton(R.string.copy) { dialog, _ ->
                copyToClipboard(getString(R.string.wallet_code_clipboard), mCoin.publicKey)
                dialog.cancel()
                Snackbar.make(container, R.string.wallet_code_clipboard, Snackbar.LENGTH_LONG).show()
            }
            .create()
        dialog.show()

        dialog.findViewById<AppCompatTextView>(R.id.wallet_code)?.text = mCoin.publicKey

        val walletQrCode =
            BarcodeEncoder().encodeBitmap(mCoin.publicKey, BarcodeFormat.QR_CODE, 200, 200)
        dialog.findViewById<AppCompatImageView>(R.id.wallet_qr_code)?.setImageBitmap(walletQrCode)

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
