package com.app.belcobtm.presentation.features.wallet.trade.recall

import android.os.Bundle
import androidx.lifecycle.Observer
import com.app.belcobtm.R
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.presentation.core.extensions.*
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.ui.BaseActivity
import com.app.belcobtm.presentation.core.watcher.DoubleTextWatcher
import com.app.belcobtm.presentation.features.wallet.trade.reserve.InputFieldState
import kotlinx.android.synthetic.main.activity_trade_recall.*
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class TradeRecallActivity : BaseActivity() {
    private val viewModel: TradeRecallViewModel by viewModel {
        parametersOf(intent.getStringExtra(TAG_COIN_CODE))
    }
    private val doubleTextWatcher: DoubleTextWatcher = DoubleTextWatcher(
        maxCharsAfterDotFirst = DoubleTextWatcher.MAX_CHARS_AFTER_DOT_CRYPTO,
        maxCharsAfterDotSecond = DoubleTextWatcher.MAX_CHARS_AFTER_DOT_USD,
        firstTextWatcher = {
            val cryptoAmount = it.getDouble()
            val usdAmount = cryptoAmount * viewModel.coinItem.priceUsd
            if (cryptoAmount > 0) {
                amountUsdView.setText(usdAmount.toStringUsd())
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
        setContentView(R.layout.activity_trade_recall)

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
        recallButtonView.setOnClickListener { viewModel.performTransaction() }
    }

    private fun initObservers() {
        viewModel.initialLoadLiveData.observe(this, Observer { initialLoadData ->
            when (initialLoadData) {
                is LoadingData.Loading -> {
                    progressView.show()
                    recallContent.hide()
                }
                is LoadingData.Success -> {
                    progressView.hide()
                    recallContent.show()
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
        viewModel.transactionLiveData.observe(this, Observer { loadingData ->
            when (loadingData) {
                is LoadingData.Loading -> progressView.show()
                is LoadingData.Success -> finish()
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
                    getString(R.string.trade_recall_screen_min_error)
                InputFieldState.MoreThanNeedError -> amountCryptoView.error =
                    getString(R.string.trade_recall_screen_max_error)
                InputFieldState.NotEnoughETHError -> amountUsdView.error =
                    getString(R.string.trade_recall_screen_not_enough_eth)
            }
        })
        viewModel.usdFieldState.observe(this, Observer { fieldState ->
            when (fieldState) {
                InputFieldState.Valid -> amountUsdView.clearError()
                InputFieldState.LessThanNeedError -> amountUsdView.error =
                    getString(R.string.trade_recall_screen_min_error)
                InputFieldState.MoreThanNeedError -> amountUsdView.error =
                    getString(R.string.trade_recall_screen_max_error)
                InputFieldState.NotEnoughETHError -> amountUsdView.error =
                    getString(R.string.trade_recall_screen_not_enough_eth)
            }
        })
        viewModel.submitButtonEnable.observe(this, Observer { enable ->
            recallButtonView.isEnabled = enable
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
