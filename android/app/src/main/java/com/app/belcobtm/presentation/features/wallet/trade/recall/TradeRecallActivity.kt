package com.app.belcobtm.presentation.features.wallet.trade.recall

import android.os.Bundle
import com.app.belcobtm.R
import com.app.belcobtm.presentation.core.extensions.*
import com.app.belcobtm.presentation.core.ui.BaseActivity
import com.app.belcobtm.presentation.core.watcher.DoubleTextWatcher
import kotlinx.android.synthetic.main.activity_trade_recall.*
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class TradeRecallActivity : BaseActivity() {
    private val viewModel: TradeRecallViewModel by viewModel { parametersOf(intent.getStringExtra(TAG_COIN_CODE)) }
    private val doubleTextWatcher: DoubleTextWatcher = DoubleTextWatcher(
        maxCharsAfterDotFirst = DoubleTextWatcher.MAX_CHARS_AFTER_DOT_CRYPTO,
        maxCharsAfterDotSecond = DoubleTextWatcher.MAX_CHARS_AFTER_DOT_USD,
        firstTextWatcher = {
            val cryptoBalance = viewModel.getMaxValue()
            val cryptoAmountTemporary = it.getDouble()
            val cryptoAmount: Double

            if (cryptoAmountTemporary > cryptoBalance) {
                viewModel.selectedAmount = viewModel.coinItem.balanceCoin
                cryptoAmount = cryptoBalance
                it.clear()
                it.insert(0, cryptoAmount.toStringCoin())
            } else {
                viewModel.selectedAmount = cryptoAmountTemporary
                cryptoAmount = cryptoAmountTemporary
            }

            if (cryptoAmountTemporary > 0) {
                amountUsdView.setText((cryptoAmount * viewModel.coinItem.priceUsd).toStringUsd())
                recallButtonView.isEnabled = true
            } else {
                amountUsdView.clearText()
                recallButtonView.isEnabled = false
            }
        },
        secondTextWatcher = {
            val maxCryptoAmount = viewModel.getMaxValue()
            val maxUsdAmount = maxCryptoAmount * viewModel.coinItem.priceUsd
            val usdAmountTemporary = it.getDouble()
            val usdAmount: Double

            if (usdAmountTemporary > maxUsdAmount) {
                viewModel.selectedAmount = viewModel.coinItem.balanceCoin
                usdAmount = maxUsdAmount
                it.clear()
                it.insert(0, usdAmount.toStringUsd())
            } else {
                viewModel.selectedAmount = usdAmountTemporary / viewModel.coinItem.priceUsd
                usdAmount = usdAmountTemporary
            }

            if (usdAmountTemporary > 0) {
                amountCryptoView.setText((usdAmount / viewModel.coinItem.priceUsd).toStringCoin())
                recallButtonView.isEnabled = true
            } else {
                amountCryptoView.clearText()
                recallButtonView.isEnabled = false
            }
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trade_recall)

        initListeners()
        initObservers()
        initViews()
    }

    private fun initListeners() {
        maxCryptoView.setOnClickListener { amountCryptoView.setText(viewModel.getMaxValue().toStringCoin()) }
        maxUsdView.setOnClickListener { amountCryptoView.setText(viewModel.getMaxValue().toStringCoin()) }
        amountCryptoView.editText?.addTextChangedListener(doubleTextWatcher.firstTextWatcher)
        amountUsdView.editText?.addTextChangedListener(doubleTextWatcher.secondTextWatcher)
//        amountCryptoView.actionDoneListener { validateAndSubmit() }
//        nextButtonView.setOnClickListener { validateAndSubmit() }
    }

    private fun initObservers() {
    }

    private fun initViews() {
        priceUsdView.text = getString(R.string.transaction_price_usd, viewModel.coinItem.priceUsd.toStringUsd())
        balanceCryptoView.text = getString(
            R.string.transaction_crypto_balance,
            viewModel.coinItem.balanceCoin.toStringCoin(),
            viewModel.coinItem.code
        )
        balanceUsdView.text = getString(R.string.transaction_price_usd, viewModel.coinItem.balanceUsd.toStringUsd())
        reservedCryptoView.text = getString(
            R.string.exchange_coin_to_coin_screen_balance_crypto,
            viewModel.coinItem.reservedBalanceCoin.toStringCoin(),
            viewModel.coinItem.code
        )
        reservedUsdView.text = getString(
            R.string.exchange_coin_to_coin_screen_balance_usd,
            viewModel.coinItem.reservedBalanceUsd.toStringUsd()
        )
        amountCryptoView.hint = getString(R.string.withdraw_screen_crypto_amount, viewModel.coinItem.code)
    }

    companion object {
        const val TAG_COIN_CODE = "tag_coin_code"
    }
}