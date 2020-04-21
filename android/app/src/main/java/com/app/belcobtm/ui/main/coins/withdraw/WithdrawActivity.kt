package com.app.belcobtm.ui.main.coins.withdraw

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
    private lateinit var mCoin: CoinModel
    var cryptoBalanceToSend = 0.0

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
        amountUsdView?.editText?.keyListener = null
        amountCryptoView.editText?.addTextChangedListener(coinFromTextWatcher)
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
        amountCryptoView.actionDoneListener { validateAndSubmit() }
        nextButtonView.setOnClickListener { validateAndSubmit() }
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
            mPresenter.getCoinTransactionHash(mCoin.coinId, toAddress, cryptoBalanceToSend)
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

    private val coinFromTextWatcher = object : TextWatcher {
        val dotChar: Char = '.'
        var isRunning = false
        var isDeleting = false

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            isDeleting = count > after
        }

        override fun onTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

        override fun afterTextChanged(editable: Editable) {
            if (isRunning) return

            isRunning = true

            when {
                editable.isEmpty() || editable.toString().replace(
                    dotChar.toString(),
                    ""
                ).toInt() <= 0 -> {
                    when {
                        editable.contains(dotChar) && editable.indexOf(dotChar, 0, false) > 1 ->
                            editable.delete(0, editable.indexOf(dotChar, 0, false) - 1)
                        !editable.contains(dotChar) && editable.length > 1 -> editable.delete(0, editable.length - 1)
                    }
                    amountUsdView.clearText()
                }
                editable.first() == dotChar -> editable.insert(0, "0")
                editable.last() == dotChar && editable.count { it == dotChar } > 1 -> editable.delete(
                    editable.lastIndex,
                    editable.length
                )
                else -> {
                    val cryptoBalance = mCoin.balance - mPresenter.getTransactionFee(mCoin.coinId)
                    val cryptoAmountTemporary = amountCryptoView.getString().toDouble()
                    val cryptoAmount: Double =
                        if (cryptoAmountTemporary > cryptoBalance) cryptoBalance
                        else cryptoAmountTemporary

                    editable.clear()
                    editable.insert(0, cryptoAmount.toStringCoin())
                    amountUsdView.setText((cryptoAmount * mCoin.price.uSD).toStringUsd())

                    cryptoBalanceToSend = cryptoAmount
                }
            }

            isRunning = false
        }
    }

    companion object {
        private const val KEY_COIN = "KEY_COIN"

        @JvmStatic
        fun start(context: Context?, coin: CoinModel) {
            val intent = Intent(context, WithdrawActivity::class.java)
            intent.putExtra(KEY_COIN, Parcels.wrap(coin))
            context?.startActivity(intent)
        }
    }
}
