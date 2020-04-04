package com.app.belcobtm.ui.main.coins.withdraw

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.widget.doAfterTextChanged
import com.app.belcobtm.R
import com.app.belcobtm.api.model.response.CoinModel
import com.app.belcobtm.mvp.BaseMvpActivity
import com.app.belcobtm.presentation.core.extensions.*
import com.google.android.material.textfield.TextInputLayout
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.activity_withdraw.*
import org.parceler.Parcels

class WithdrawActivity : BaseMvpActivity<WithdrawContract.View, WithdrawContract.Presenter>(),
    WithdrawContract.View {

    companion object {
        private const val KEY_COIN = "KEY_COIN"

        @JvmStatic
        fun start(context: Context?, coin: CoinModel) {
            val intent = Intent(context, WithdrawActivity::class.java)
            intent.putExtra(KEY_COIN, Parcels.wrap(coin))
            context?.startActivity(intent)
        }
    }

    private lateinit var mCoin: CoinModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_withdraw)
        setSupportActionBar(toolbarView)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        mCoin = Parcels.unwrap(intent.getParcelableExtra(KEY_COIN))

        supportActionBar?.title = getString(R.string.title_withdraw) + " " + mCoin.coinId

        initListeners()
        initViews()
    }

    private fun initListeners() {
        addressScanView.setOnClickListener { IntentIntegrator(this).initiateScan() }
        addressPasteView.setOnClickListener { addressView.setText(getTextFromClipboard()) }
        maxCryptoView.setOnClickListener {
            //TODO
            val balance = mCoin.balance - mPresenter.getTransactionFee(mCoin.coinId)
            val balanceStr = if (balance > 0) {
                cryptoBalanceToSend = balance
                String.format("%.6f", balance).trimEnd('0')
            } else {
                cryptoBalanceToSend = 0.0
                "0"
            }
            amountCryptoView.setText(balanceStr.replace(',', '.'))
        }
        maxUsdView.setOnClickListener {
            val balance = mCoin.balance - mPresenter.getTransactionFee(mCoin.coinId)
            val balanceStr = if (balance > 0) {
                cryptoBalanceToSend = balance
                String.format("%.6f", balance).trimEnd('0')
            } else {
                cryptoBalanceToSend = 0.0
                "0"
            }
            amountCryptoView.setText(balanceStr.replace(',', '.'))
        }
    }

    private fun initPrice() {
        val convertedPrice = if (mCoin.price.uSD > 0) String.format("%.2f", mCoin.price.uSD).trimEnd('0') else "0"
        priceUsdView.text = getString(R.string.transaction_price_usd, convertedPrice)
    }

    private fun initBalance() {
        val convertedBalance = if (mCoin.balance > 0) String.format("%.6f", mCoin.balance).trimEnd('0') else "0"
        balanceCryptoView.text = getString(R.string.transaction_crypto_balance, convertedBalance, mCoin.coinId)

        val amountUsd = mCoin.balance * mCoin.price.uSD
        balanceUsdView.text = "${String.format("%.2f", amountUsd)} USD"
    }


    private fun initViews() {
        initPrice()
        initBalance()

        amountCryptoView.hint = mCoin.coinId
        handleAmount()

        amountCryptoView.actionDoneListener { validateAndSubmit() }
        nextButtonView.setOnClickListener { validateAndSubmit() }
    }

    var cryptoBalanceToSend = 0.0

    private fun handleAmount() {
        var isTextWorking = false
        amountCryptoView?.editText?.doAfterTextChanged {
            if (isTextWorking)
                return@doAfterTextChanged
            isTextWorking = true
            var balance = mCoin.balance - mPresenter.getTransactionFee(mCoin.coinId)
            balance = if (balance < 0) 0.0 else balance


            val amountCrypto = try {
                amountCryptoView.getString().toDouble()
            } catch (e: NumberFormatException) {
                0.0
            }
            cryptoBalanceToSend = amountCrypto

            if (amountCrypto > balance) {
                amountCryptoView.setText(trimTrailingZero(balance.toString()) ?: "")
                cryptoBalanceToSend = balance
                isTextWorking = false
            }
            val amountUsd = amountCrypto * mCoin.price.uSD
            amountUsdView.setText(String.format("%.2f", amountUsd))
            amountUsdView.editText?.setSelection(amountUsdView.getString().length)
            isTextWorking = false
        }

        amountUsdView.editText?.doAfterTextChanged {
            if (isTextWorking)
                return@doAfterTextChanged
            isTextWorking = true
            var balance = mCoin.balance - mPresenter.getTransactionFee(mCoin.coinId)
            balance = if (balance < 0) 0.0 else balance
            if (balance == 0.0) {
                amountUsdView.setText("0")
                amountUsdView?.editText?.setSelection(amountUsdView.getString().length)
                amountCryptoView.setText("0")
                amountCryptoView?.editText?.setSelection(amountCryptoView.getString().length)
                isTextWorking = false
                return@doAfterTextChanged
            }

            val maxUsd =
                (mCoin.balance - mPresenter.getTransactionFee(mCoin.coinId)) * mCoin.price.uSD
            val amountUsd = try {
                amountUsdView.getString().toDouble()
            } catch (e: NumberFormatException) {
                0.0
            }

            if (amountUsd > maxUsd) {
                amountUsdView.setText(String.format("%.2f", maxUsd))
                amountUsdView?.editText?.setSelection(amountUsdView.getString().length)
            }
            var amountCrypt = try {
                amountUsdView.getString().toDouble() / mCoin.price.uSD
            } catch (e: NumberFormatException) {
                0.0
            }

            amountCrypt = if (amountCrypt < 0) 0.0 else amountCrypt
            amountCryptoView.setText(trimTrailingZero(String.format("%.6f", amountCrypt)) ?: "")
            amountCryptoView?.editText?.setSelection(amountCryptoView.getString().length)
            isTextWorking = false
        }
    }

    fun trimTrailingZero(value: String?): String? {
        return if (!value.isNullOrEmpty()) {
            if (value!!.indexOf(".") < 0) {
                value

            } else {
                value.replace("0*$".toRegex(), "").replace("\\.$".toRegex(), "")
            }

        } else {
            value
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getTextFromClipboard(): String {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = clipboard.primaryClip
        val item = clipData?.getItemAt(0)
        return item?.text.toString()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                showError(R.string.cancelled)
            } else {
                val walletCode = result.contents.replaceBefore(':', "")
                    .replaceBefore('=', "")
                    .removePrefix(":")
                addressView.setText(walletCode)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun validateAndSubmit() {
        amountCryptoView.clearError()
        addressView.clearError()

        val toAddress = addressView.getString()
        var errors = 0

        //Validate address
        if (!mPresenter.validateAddress(mCoin.coinId, toAddress)) {
            errors++
            addressView.showError(R.string.wrong_address)
        }

        //Validate amount
        if (cryptoBalanceToSend <= 0) {
            errors++
            amountCryptoView.showError(R.string.should_be_filled)
        }

        //Validate max amount
        if (cryptoBalanceToSend > (mCoin.balance - mPresenter.getTransactionFee(mCoin.coinId))) {
            errors++
            amountCryptoView.showError(R.string.not_enough_balance)
        }

        if (errors == 0) {
            mPresenter.getCoinTransactionHash(this, mCoin.coinId, toAddress, cryptoBalanceToSend)
        }
    }

    override fun onTransactionDone() {
        showMessage("Transaction Done")
        finish()
    }

    override fun openSmsCodeDialog(error: String?) {
        val view = layoutInflater.inflate(R.layout.view_sms_code_dialog, null)
        val smsCode = view.findViewById<AppCompatEditText>(R.id.sms_code)
        AlertDialog
            .Builder(this)
            .setTitle(getString(R.string.verify_sms_code))
            .setPositiveButton(R.string.next)
            { _, _ ->
                val code = smsCode.text.toString()
                if (code.length != 4) {
                    openSmsCodeDialog(getString(R.string.error_sms_code_4_digits))
                } else {
                    mPresenter.verifySmsCode(code)
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .setView(view)
            .create()
            .show()
        val tilSmsCode = view.findViewById<TextInputLayout>(R.id.til_sms_code)
        tilSmsCode.error = error
    }
}
