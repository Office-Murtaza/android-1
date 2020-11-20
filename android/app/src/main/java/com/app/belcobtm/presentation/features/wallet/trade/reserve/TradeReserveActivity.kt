package com.app.belcobtm.presentation.features.wallet.trade.reserve

import android.os.Bundle
import androidx.lifecycle.Observer
import com.app.belcobtm.R
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.presentation.core.extensions.*
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.ui.BaseActivity
import com.app.belcobtm.presentation.core.watcher.DoubleTextWatcher
import kotlinx.android.synthetic.main.activity_trade_reserve.*
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class TradeReserveActivity : BaseActivity() {
    private val viewModel: TradeReserveViewModel by viewModel {
        parametersOf(intent.getStringExtra(TAG_COIN_CODE))
    }
    private val doubleTextWatcher: DoubleTextWatcher = DoubleTextWatcher(
        maxCharsAfterDotFirst = DoubleTextWatcher.MAX_CHARS_AFTER_DOT_CRYPTO,
        maxCharsAfterDotSecond = DoubleTextWatcher.MAX_CHARS_AFTER_DOT_USD,
        firstTextWatcher = {
            val cryptoAmount = it.getDouble()
            if (cryptoAmount > 0) {
                amountUsdView.setText((cryptoAmount * viewModel.coinItem.priceUsd).toStringUsd())
            } else {
                amountUsdView.clearText()
            }
            viewModel.validateCryptoAmount(cryptoAmount)
        },
        secondTextWatcher = {
            val usdAmount = it.getDouble()
            val cryptoAmount = usdAmount / viewModel.coinItem.priceUsd
            if (usdAmount > 0) {
                amountCryptoView.setText(cryptoAmount.toStringCoin())
            } else {
                amountCryptoView.clearText()
            }
            viewModel.validateCryptoAmount(cryptoAmount)
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trade_reserve)

        initListeners()
        initObservers()
        initViews()
    }

    private fun initListeners() {
        maxCryptoView.setOnClickListener {
            amountCryptoView.setText(
                viewModel.getMaxValue().toStringCoin()
            )
        }
        maxUsdView.setOnClickListener {
            amountCryptoView.setText(
                viewModel.getMaxValue().toStringCoin()
            )
        }
        amountCryptoView.editText?.addTextChangedListener(doubleTextWatcher.firstTextWatcher)
        amountUsdView.editText?.addTextChangedListener(doubleTextWatcher.secondTextWatcher)
        reserveButtonView.setOnClickListener { viewModel.createTransaction() }
    }

    private fun initObservers() {
        viewModel.initialLoadLiveData.observe(this, Observer { initialLoadData ->
            when (initialLoadData) {
                is LoadingData.Loading -> {
                    progressView.show()
                    reserveContent.hide()
                }
                is LoadingData.Success -> {
                    progressView.hide()
                    reserveContent.show()
                }
                is LoadingData.Error -> {
                    progressView.hide()
                    // do not show content
                    when (initialLoadData.errorType) {
                        is Failure.NetworkConnection -> showError(R.string.error_internet_unavailable)
                        else -> showError(R.string.error_something_went_wrong)
                    }
                }
            }
        })
        viewModel.createTransactionLiveData.observe(this, Observer { loadingData ->
            when (loadingData) {
                is LoadingData.Loading -> progressView.show()
                is LoadingData.Success -> {
                    progressView.hide()
                    finish()
                }
                is LoadingData.Error -> {
                    when (loadingData.errorType) {
                        is Failure.NetworkConnection -> showError(R.string.error_internet_unavailable)
                        else -> showError(R.string.error_something_went_wrong)
                    }
                    progressView.hide()
                }
            }
        })
        viewModel.cryptoFieldState.observe(this, Observer { fieldState ->
            when (fieldState) {
                InputFieldState.Valid -> amountCryptoView.clearError()
                InputFieldState.LessThanNeedError -> amountCryptoView.error =
                    getString(R.string.trade_reserve_screen_min_error)
                InputFieldState.MoreThanNeedError -> amountCryptoView.error =
                    getString(R.string.trade_reserve_screen_max_error)
                InputFieldState.NotEnoughETHError -> amountUsdView.error =
                    getString(R.string.trade_reserve_screen_not_enough_eth)
            }
        })
        viewModel.usdFieldState.observe(this, Observer { fieldState ->
            when (fieldState) {
                InputFieldState.Valid -> amountUsdView.clearError()
                InputFieldState.LessThanNeedError -> amountUsdView.error =
                    getString(R.string.trade_reserve_screen_min_error)
                InputFieldState.MoreThanNeedError -> amountUsdView.error =
                    getString(R.string.trade_reserve_screen_max_error)
                InputFieldState.NotEnoughETHError -> amountUsdView.error =
                    getString(R.string.trade_reserve_screen_not_enough_eth)
            }
        })
        viewModel.submitButtonEnable.observe(this, Observer { enable ->
            reserveButtonView.isEnabled = enable
        })
    }

    private fun initViews() {
        setSupportActionBar(toolbarView)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        priceUsdView.text = getString(R.string.text_usd, viewModel.coinItem.priceUsd.toStringUsd())
        balanceCryptoView.text = getString(
            R.string.text_text,
            viewModel.coinItem.balanceCoin.toStringCoin(),
            viewModel.coinItem.code
        )
        balanceUsdView.text =
            getString(R.string.text_usd, viewModel.coinItem.balanceUsd.toStringUsd())
        reservedCryptoView.text = getString(
            R.string.text_text,
            viewModel.coinItem.reservedBalanceCoin.toStringCoin(),
            viewModel.coinItem.code
        )
        reservedUsdView.text = getString(
            R.string.text_usd,
            viewModel.coinItem.reservedBalanceUsd.toStringUsd()
        )
        amountCryptoView.hint = getString(R.string.text_amount, viewModel.coinItem.code)
    }

    companion object {
        const val TAG_COIN_CODE = "tag_coin_code"
    }
}