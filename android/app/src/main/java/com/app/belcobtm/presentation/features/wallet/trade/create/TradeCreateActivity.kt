package com.app.belcobtm.presentation.features.wallet.trade.create

import android.os.Bundle
import androidx.lifecycle.Observer
import com.app.belcobtm.R
import com.app.belcobtm.databinding.ActivityTradeCreateBinding
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.presentation.core.extensions.*
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.ui.BaseActivity
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class TradeCreateActivity : BaseActivity() {

    private val viewModel: TradeCreateViewModel by viewModel { parametersOf(intent.getStringExtra(TAG_COIN_CODE)) }
    private lateinit var binding: ActivityTradeCreateBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTradeCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.initListeners()
        binding.initObservers()
        binding.initViews()
    }

    private fun ActivityTradeCreateBinding.initListeners() {
        createButtonView.setOnClickListener { createTrade() }
        tradeTermsView.actionDoneListener { createTrade() }
    }

    private fun ActivityTradeCreateBinding.initObservers() {
        viewModel.loadingData.observe(this@TradeCreateActivity, Observer { loadingData ->
            when (loadingData) {
                is LoadingData.Loading -> progressView.show()
                is LoadingData.Success -> progressView.hide()
                is LoadingData.Error -> {
                    when (loadingData.errorType) {
                        is Failure.MessageError -> showError(loadingData.errorType.message)
                        is Failure.NetworkConnection -> showError(R.string.error_internet_unavailable)
                        else -> showError(R.string.error_something_went_wrong)
                    }
                    progressView.hide()
                }
            }
        })
        viewModel.createTradeLiveData.observe(this@TradeCreateActivity, Observer { loadingData ->
            when (loadingData) {
                is LoadingData.Loading -> progressView.show()
                is LoadingData.Success -> {
                    progressView.hide()
                    finish()
                }
                is LoadingData.Error -> {
                    when (loadingData.errorType) {
                        is Failure.MessageError -> showError(loadingData.errorType.message)
                        is Failure.NetworkConnection -> showError(R.string.error_internet_unavailable)
                        else -> showError(R.string.error_something_went_wrong)
                    }
                    progressView.hide()
                }
            }
        })
    }

    private fun ActivityTradeCreateBinding.initViews() {
        setSupportActionBar(toolbarView)
        supportActionBar?.let { toolbar ->
            toolbar.setDisplayHomeAsUpEnabled(true)
            toolbar.setDisplayShowHomeEnabled(true)
        }
        with(viewModel) {
            toolbarView.title = getString(R.string.add_edit_trade_screen_title_create, fromCoinItem.code)
            priceUsdView.text = getString(R.string.text_usd, fromCoinItem.priceUsd.toStringUsd())
            balanceCryptoView.text = getString(
                R.string.text_text,
                fromCoinItem.balanceCoin.toStringCoin(),
                fromCoinItem.code
            )
            balanceUsdView.text = getString(
                R.string.text_usd,
                fromCoinItem.balanceUsd.toStringUsd()
            )
            reservedCryptoView.text = getString(
                R.string.text_text,
                fromCoinItem.reservedBalanceCoin.toStringCoin(),
                fromCoinItem.code
            )
            reservedUsdView.text = getString(
                R.string.text_usd,
                fromCoinItem.reservedBalanceUsd.toStringUsd()
            )
        }
    }

    private fun ActivityTradeCreateBinding.createTrade() {
        when {
            isEmptyFields() -> showError(R.string.error_please_fill_all_fields)
            !isCorrectLimits() -> showError(R.string.add_edit_trade_screen_max_min_limit_error)
            !isCorrectMargin() -> showError(R.string.add_edit_trade_screen_margin_error)
            else -> viewModel.createTrade(
                buyButtonView.isChecked,
                paymentMethodView.getString(),
                marginView.getString().toDouble(),
                minLimitView.getString().toLong(),
                maxLimitView.getString().toLong(),
                tradeTermsView.getString()
            )
        }
    }

    private fun ActivityTradeCreateBinding.isEmptyFields(): Boolean = paymentMethodView.getString().isEmpty()
            || marginView.getString().isEmpty()
            || minLimitView.getString().isEmpty()
            || maxLimitView.getString().isEmpty()
            || tradeTermsView.getString().isEmpty()

    private fun isCorrectLimits(): Boolean =
        binding.minLimitView.getString().toInt() < binding.maxLimitView.getString().toInt()

    private fun isCorrectMargin(): Boolean = binding.marginView.getString().toDouble() <= 100

    companion object {
        const val TAG_COIN_CODE = "tag_coin_code"
    }
}