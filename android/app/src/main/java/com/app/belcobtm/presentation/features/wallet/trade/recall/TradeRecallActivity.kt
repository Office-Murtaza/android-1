package com.app.belcobtm.presentation.features.wallet.trade.recall

import android.content.Intent
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import com.app.belcobtm.R
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.presentation.core.extensions.*
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.ui.BaseActivity
import com.app.belcobtm.presentation.core.ui.SmsDialogFragment
import com.app.belcobtm.presentation.core.watcher.DoubleTextWatcher
import com.app.belcobtm.presentation.features.HostActivity
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
        recallButtonView.setOnClickListener {
            if (viewModel.isEnoughReservedAmount()) {
                viewModel.createTransaction()
            } else {
                showError(R.string.trade_reserve_screen_not_enough_reserved_amount)
            }
        }
    }

    private fun initObservers() {
        viewModel.createTransactionLiveData.observe(this, Observer { loadingData ->
            when (loadingData) {
                is LoadingData.Loading -> progressView.show()
                is LoadingData.Success -> {
                    showSmsDialog()
                    progressView.hide()
                }
                is LoadingData.Error -> {
                    when (loadingData.errorType) {
                        is Failure.TokenError -> {
                            val intent = Intent(this, HostActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            startActivity(intent)
                        }
                        is Failure.MessageError -> showError(loadingData.errorType.message)
                        is Failure.NetworkConnection -> showError(R.string.error_internet_unavailable)
                        else -> showError(R.string.error_something_went_wrong)
                    }
                    progressView.hide()
                }
            }
        })

        viewModel.completeTransactionLiveData.observe(this, Observer
        { loadingData ->
            when (loadingData) {
                is LoadingData.Loading -> progressView.show()
                is LoadingData.Success -> finish()
                is LoadingData.Error -> {
                    when (loadingData.errorType) {
                        is Failure.TokenError -> {
                            val intent = Intent(this, HostActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            startActivity(intent)
                        }
                        is Failure.MessageError -> showSmsDialog(loadingData.errorType.message)
                        is Failure.NetworkConnection -> showError(R.string.error_internet_unavailable)
                        else -> showError(R.string.error_something_went_wrong)
                    }
                    progressView.hide()
                }
            }
        })
    }

    private fun initViews() {
        setSupportActionBar(toolbarView)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
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

    private fun showSmsDialog(errorMessage: String? = null) {
        val fragment = SmsDialogFragment()
        fragment.arguments = bundleOf(SmsDialogFragment.TAG_ERROR to errorMessage)
        fragment.show(supportFragmentManager, SmsDialogFragment::class.simpleName)
        fragment.setDialogListener { smsCode -> viewModel.completeTransaction(smsCode) }
    }

    companion object {
        const val TAG_COIN_CODE = "tag_coin_code"
    }
}