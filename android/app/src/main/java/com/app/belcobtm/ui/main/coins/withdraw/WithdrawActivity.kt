package com.app.belcobtm.ui.main.coins.withdraw

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.widget.doAfterTextChanged
import com.app.belcobtm.R
import com.app.belcobtm.api.model.response.CoinModel
import com.app.belcobtm.mvp.BaseMvpActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.activity_show_phone.toolbar
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
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        mCoin = Parcels.unwrap(intent.getParcelableExtra(KEY_COIN))

        supportActionBar?.title = getString(R.string.title_withdraw) + " " + mCoin.coinId

        initView()
    }

    private fun initView() {

        address_scan.setOnClickListener { IntentIntegrator(this).initiateScan() }
        address_paste.setOnClickListener { address.setText(getTextFromClipboard()) }
        amount_max.setOnClickListener {
            val balance = mCoin.balance - mPresenter.getTransactionFee(mCoin.coinId)
            val balanceStr = if (balance > 0) {
                cryptoBalanceToSend = balance
                String.format("%.6f", balance).trimEnd('0')
            } else {
                cryptoBalanceToSend = 0.0
                "0"
            }
            amount_crypto.setText(balanceStr.replace(',', '.'))
        }

        til_amount_crypto.hint = mCoin.coinId
        handleAmount()

        amount_crypto.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                validateAndSubmit()
                return@OnEditorActionListener true
            }
            false
        })

        nextButtonView.setOnClickListener { validateAndSubmit() }
    }

    var cryptoBalanceToSend = 0.0

    private fun handleAmount() {
        var isTextWorking = false
        amount_crypto.doAfterTextChanged {
            if (isTextWorking)
                return@doAfterTextChanged
            isTextWorking = true
            var balance = mCoin.balance - mPresenter.getTransactionFee(mCoin.coinId)
            balance = if (balance < 0) 0.0 else balance


            val amountCrypto = try {
                amount_crypto.text.toString().toDouble()
            } catch (e: NumberFormatException) {
                0.0
            }
            cryptoBalanceToSend = amountCrypto

            if (amountCrypto > balance) {
                amount_crypto.setText(trimTrailingZero(balance.toString()))
                cryptoBalanceToSend = balance
                isTextWorking = false
            }
            val amountUsd = amountCrypto * mCoin.price.uSD
            amount_usd.setText(String.format("%.2f", amountUsd))
            amount_usd.setSelection(amount_usd.text?.length ?: 0)
            isTextWorking = false
        }

        amount_usd.doAfterTextChanged {
            if (isTextWorking)
                return@doAfterTextChanged
            isTextWorking = true
            var balance = mCoin.balance - mPresenter.getTransactionFee(mCoin.coinId)
            balance = if (balance < 0) 0.0 else balance
            if (balance == 0.0) {
                amount_usd.setText("0")
                amount_usd.setSelection(amount_usd.text?.length ?: 0)
                amount_crypto.setText("0")
                amount_crypto.setSelection(amount_crypto.text?.length ?: 0)
                isTextWorking = false
                return@doAfterTextChanged
            }

            val maxUsd =
                (mCoin.balance - mPresenter.getTransactionFee(mCoin.coinId)) * mCoin.price.uSD
            val amountUsd = try {
                amount_usd.text.toString().toDouble()
            } catch (e: NumberFormatException) {
                0.0
            }

            if (amountUsd > maxUsd) {
                amount_usd.setText(String.format("%.2f", maxUsd))
                amount_usd.setSelection(amount_usd.text?.length ?: 0)
            }
            var amountCrypt = try {
                amount_usd.text.toString().toDouble() / mCoin.price.uSD
            } catch (e: NumberFormatException) {
                0.0
            }

            amountCrypt = if (amountCrypt < 0) 0.0 else amountCrypt
            amount_crypto.setText(String.format("%.6f", amountCrypt))
            amount_crypto.setText(trimTrailingZero(amount_crypto.text.toString()))
            amount_crypto.setSelection(amount_crypto.text?.length ?: 0)
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
                address.setText(walletCode)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun validateAndSubmit() {
        til_amount_crypto.error = null
        til_address.error = null

        val toAddress = address.text.toString()
        var errors = 0

        //Validate address
        if (!mPresenter.validateAddress(mCoin.coinId, toAddress)) {
            errors++
            til_address.error = getString(R.string.wrong_address)
        }

        //Validate amount
        if (cryptoBalanceToSend <= 0) {
            errors++
            til_amount_crypto.error = getString(R.string.should_be_filled)
        }

        //Validate max amount
        if (cryptoBalanceToSend > (mCoin.balance - mPresenter.getTransactionFee(mCoin.coinId))) {
            errors++
            til_amount_crypto.error = "Not enough balance"
        }

        if (errors == 0) {
            mPresenter.getCoinTransactionHash(this, mCoin.coinId, toAddress, cryptoBalanceToSend!!)
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
