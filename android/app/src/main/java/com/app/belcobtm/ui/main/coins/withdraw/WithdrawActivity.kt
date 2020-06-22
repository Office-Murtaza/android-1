package com.app.belcobtm.ui.main.coins.withdraw

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import com.app.belcobtm.R
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.domain.wallet.item.CoinDataItem
import com.app.belcobtm.mvp.BaseMvpActivity
import com.app.belcobtm.presentation.core.extensions.*
import com.app.belcobtm.presentation.core.watcher.DoubleTextWatcher
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.activity_withdraw.*
import kotlinx.android.synthetic.main.view_sms_code_dialog.view.*

class WithdrawActivity : BaseMvpActivity<WithdrawContract.View, WithdrawContract.Presenter>(),
    WithdrawContract.View {
    private lateinit var coinDataItem: CoinDataItem
    private lateinit var coinDataItemList: List<CoinDataItem>
    private var cryptoBalanceToSend = 0.0
    private val doubleTextWatcher: DoubleTextWatcher = DoubleTextWatcher(
        maxCharsAfterDotFirst = DoubleTextWatcher.MAX_CHARS_AFTER_DOT_CRYPTO,
        maxCharsAfterDotSecond = DoubleTextWatcher.MAX_CHARS_AFTER_DOT_USD,
        firstTextWatcher = {
            val cryptoBalance = getMaxValue()
            val cryptoAmountTemporary = it.getDouble()
            val cryptoAmount: Double

            if (cryptoAmountTemporary > cryptoBalance) {
                cryptoBalanceToSend = coinDataItem.balanceCoin
                cryptoAmount = cryptoBalance
                it.clear()
                it.insert(0, cryptoAmount.toStringCoin())
            } else {
                cryptoBalanceToSend = cryptoAmountTemporary
                cryptoAmount = cryptoAmountTemporary
            }

            if (cryptoAmountTemporary > 0) {
                amountUsdView.setText((cryptoAmount * coinDataItem.priceUsd).toStringUsd())
                nextButtonView.isEnabled = true
            } else {
                amountUsdView.clearText()
                nextButtonView.isEnabled = false
            }
        },
        secondTextWatcher = {
            val maxCryptoAmount = getMaxValue()
            val maxUsdAmount = maxCryptoAmount * coinDataItem.priceUsd
            val usdAmountTemporary = it.getDouble()
            val usdAmount: Double

            if (usdAmountTemporary > maxUsdAmount) {
                cryptoBalanceToSend = coinDataItem.balanceCoin
                usdAmount = maxUsdAmount
                it.clear()
                it.insert(0, usdAmount.toStringUsd())
            } else {
                cryptoBalanceToSend = usdAmountTemporary / coinDataItem.priceUsd
                usdAmount = usdAmountTemporary
            }

            if (usdAmountTemporary > 0) {
                amountCryptoView.setText((usdAmount / coinDataItem.priceUsd).toStringCoin())
                nextButtonView.isEnabled = true
            } else {
                amountCryptoView.clearText()
                nextButtonView.isEnabled = false
            }
        }
    )

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
        maxUsdView.setOnClickListener { amountCryptoView.setText(getMaxValue().toStringCoin()) }
        amountCryptoView.editText?.addTextChangedListener(doubleTextWatcher.firstTextWatcher)
        amountUsdView.editText?.addTextChangedListener(doubleTextWatcher.secondTextWatcher)
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
            showError(R.string.wrong_address)
        }

        val isCatm = coinDataItem.code == LocalCoinType.CATM.name
        val ethBalance = coinDataItemList.find { LocalCoinType.ETH.name == it.code }?.balanceCoin ?: 0.0
        val isNotEnoughBalanceETH = ethBalance < mPresenter.getTransactionFee(coinDataItem.code)
        //Validate CATM by ETH commission
        if (isCatm && isNotEnoughBalanceETH) {
            errors++
            showError(R.string.withdraw_screen_where_money_libovski)
        }

        if (!isCatm && cryptoBalanceToSend > (coinDataItem.balanceCoin - mPresenter.getTransactionFee(coinDataItem.code))) {
            errors++
            showError(R.string.not_enough_balance)
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

    private fun getMaxValue(): Double = if (coinDataItem.code == LocalCoinType.CATM.name) {
        coinDataItem.balanceCoin
    } else {
        0.0.coerceAtLeast(coinDataItem.balanceCoin - mPresenter.getTransactionFee(coinDataItem.code))
    }

    companion object {
        private const val KEY_COIN = "KEY_COIN"
        private const val KEY_COIN_LIST = "KEY_COIN_LIST"

        @JvmStatic
        fun start(context: Context?, coin: CoinDataItem?, coinList: ArrayList<CoinDataItem>?) {
            val intent = Intent(context, WithdrawActivity::class.java)
            intent.putExtra(KEY_COIN, coin)
            intent.putParcelableArrayListExtra(KEY_COIN_LIST, coinList)
            context?.startActivity(intent)
        }
    }
}
