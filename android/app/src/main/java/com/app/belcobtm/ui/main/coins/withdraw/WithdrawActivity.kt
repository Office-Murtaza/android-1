package com.app.belcobtm.ui.main.coins.withdraw

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import com.app.belcobtm.R
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.domain.wallet.item.CoinDataItem
import com.app.belcobtm.mvp.BaseMvpActivity
import com.app.belcobtm.presentation.core.extensions.*
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.activity_withdraw.*
import kotlinx.android.synthetic.main.view_sms_code_dialog.view.*

class WithdrawActivity : BaseMvpActivity<WithdrawContract.View, WithdrawContract.Presenter>(),
    WithdrawContract.View {
    private lateinit var coinDataItem: CoinDataItem
    private lateinit var coinDataItemList: List<CoinDataItem>
    var cryptoBalanceToSend = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_withdraw)
        setSupportActionBar(toolbarView)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        coinDataItem = intent.getParcelableExtra(KEY_COIN) as CoinDataItem
        coinDataItemList = intent.getParcelableArrayListExtra<CoinDataItem>(KEY_COIN_LIST) as ArrayList<CoinDataItem>

        supportActionBar?.title = getString(R.string.title_withdraw) + " " + coinDataItem.code

        initListeners()
        initViews()
    }

    private fun initListeners() {
        addressScanView.setOnClickListener { IntentIntegrator(this).initiateScan() }
        addressPasteView.setOnClickListener { addressView.setText(getTextFromClipboard()) }
        maxCryptoView.setOnClickListener { amountCryptoView.setText(getMaxValue().toStringCoin()) }
        amountUsdView?.editText?.keyListener = null
        amountCryptoView.editText?.addTextChangedListener(coinFromTextWatcher)
    }

    private fun initPrice() {
        priceUsdView.text = getString(R.string.transaction_price_usd, coinDataItem.priceUsd.toStringUsd())
    }

    private fun initBalance() {
        balanceCryptoView.text =
            getString(R.string.transaction_crypto_balance, coinDataItem.balanceCoin.toStringCoin(), coinDataItem.code)
        balanceUsdView.text = getString(R.string.transaction_price_usd, coinDataItem.balanceUsd.toStringUsd())
    }

    private fun initViews() {
        initPrice()
        initBalance()

        amountCryptoView.hint = getString(R.string.withdraw_screen_crypto_amount, coinDataItem.code)
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
        if (!mPresenter.validateAddress(coinDataItem.code, toAddress)) {
            errors++
            addressView.showError(R.string.wrong_address)
        }

        //Validate amount
        if (cryptoBalanceToSend <= 0) {
            errors++
            amountCryptoView.showError(R.string.should_be_filled)
        }

        //Validate max amount
        if (cryptoBalanceToSend > (coinDataItem.balanceCoin - mPresenter.getTransactionFee(coinDataItem.code))) {
            errors++
            amountCryptoView.showError(R.string.not_enough_balance)
        }

        if (errors == 0) {
            mPresenter.getCoinTransactionHash(coinDataItem.code, toAddress, cryptoBalanceToSend)
        }
    }

    override fun onTransactionDone() {
        showMessage("Transaction Done")
        finish()
    }

    override fun openSmsCodeDialog(error: String?) {
        val view = layoutInflater.inflate(R.layout.view_sms_code_dialog, null)
        view.til_sms_code.error = error
        val dialog = AlertDialog
            .Builder(this)
            .setTitle(getString(R.string.verify_sms_code))
            .setPositiveButton(R.string.next)
            { _, _ ->
                val code = view.sms_code.text.toString()
                if (code.length != 4) {
                    openSmsCodeDialog(getString(R.string.error_sms_code_4_digits))
                } else {
                    mPresenter.verifySmsCode(code)
                }
            }
            .setNegativeButton(R.string.cancel) { _, _ -> showProgress(false) }
            .setView(view)
            .create()
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    private val coinFromTextWatcher = object : TextWatcher {
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
                editable.isNotEmpty() && editable.first() == DOT_CHAR -> editable.insert(0, "0")
                editable.isNotEmpty() && editable.last() == DOT_CHAR && editable.count { it == DOT_CHAR } > 1 -> editable.delete(
                    editable.lastIndex,
                    editable.length
                )
                editable.contains(DOT_CHAR) && (editable.lastIndex - editable.indexOf(DOT_CHAR)) > MAX_CHARS_AFTER_DOT -> editable.delete(
                    editable.lastIndex - 1,
                    editable.lastIndex
                )
                editable.isEmpty() || editable.toString().replace(DOT_CHAR.toString(), "").toInt() <= 0 -> {
                    val isContainsDot = editable.contains(DOT_CHAR)
                    val indexOfDot = editable.indexOf(DOT_CHAR)
                    when {
                        isContainsDot && indexOfDot > 1 -> editable.delete(0, indexOfDot - 1)
                        !isContainsDot && editable.length > 1 -> editable.delete(0, editable.length - 1)
                    }
                    amountUsdView.clearText()
                }
                else -> {
                    val cryptoBalance = getMaxValue()
                    val cryptoAmountTemporary = amountCryptoView.getString().toDouble()
                    val cryptoAmount: Double =
                        if (cryptoAmountTemporary > cryptoBalance) cryptoBalance
                        else cryptoAmountTemporary
                    cryptoBalanceToSend = if (cryptoAmountTemporary > cryptoBalance) coinDataItem.balanceCoin
                    else cryptoAmountTemporary

                    if (cryptoAmountTemporary > cryptoBalance) {
                        editable.clear()
                        editable.insert(0, cryptoAmount.toStringCoin())
                    }
                    amountUsdView.setText((cryptoAmount * coinDataItem.priceUsd).toStringUsd())
                }
            }

            isRunning = false
        }
    }

    private fun getMaxValue(): Double {
        val coinFee = mPresenter.getTransactionFee(coinDataItem.code)
        return if ((coinDataItem.code != LocalCoinType.CATM.name)
            || (coinDataItemList.find { LocalCoinType.ETH.name == it.code }?.balanceCoin ?: 0.0 >= coinFee)
        ) {
            coinDataItem.balanceCoin - coinFee
        } else {
            0.0
        }
    }

    companion object {
        private const val KEY_COIN = "KEY_COIN"
        private const val KEY_COIN_LIST = "KEY_COIN_LIST"
        const val MAX_CHARS_AFTER_DOT = 6
        const val DOT_CHAR: Char = '.'

        @JvmStatic
        fun start(context: Context?, coin: CoinDataItem?, coinList: ArrayList<CoinDataItem>?) {
            val intent = Intent(context, WithdrawActivity::class.java)
            intent.putExtra(KEY_COIN, coin)
            intent.putParcelableArrayListExtra(KEY_COIN_LIST, coinList)
            context?.startActivity(intent)
        }
    }
}
