package com.app.belcobtm.presentation.features.wallet.trade.create

import android.os.Bundle
import androidx.lifecycle.Observer
import com.app.belcobtm.R
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.presentation.core.extensions.*
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.ui.BaseActivity
import kotlinx.android.synthetic.main.activity_create_trade.*
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class CreateTradeActivity : BaseActivity() {
    private val viewModel: CreateTradeViewModel by viewModel { parametersOf(intent.getParcelableExtra(TAG_COIN_ITEM)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_trade)
        initListeners()
        initObservers()
        initViews()
    }

    private fun initListeners() {
        createButtonView.setOnClickListener { createTrade() }
        tradeTermsView.actionDoneListener { createTrade() }
    }

    private fun initObservers() {
        viewModel.createTradeLiveData.observe(this, Observer { loadingData ->
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

    private fun initViews() {
        setSupportActionBar(toolbarView)
        supportActionBar?.let { toolbar ->
            toolbar.setDisplayHomeAsUpEnabled(true)
            toolbar.setDisplayShowHomeEnabled(true)
        }
        with(viewModel) {
            toolbarView.title = getString(R.string.add_edit_trade_screen_title_create, fromCoinItem.code)
            priceUsdView.text = getString(R.string.unit_usd_dynamic, fromCoinItem.priceUsd.toStringUsd())
            balanceCryptoView.text = getString(
                R.string.exchange_coin_to_coin_screen_balance_crypto,
                fromCoinItem.balanceCoin.toStringCoin(),
                fromCoinItem.code
            )
            balanceUsdView.text = getString(
                R.string.exchange_coin_to_coin_screen_balance_usd,
                fromCoinItem.balanceUsd.toStringUsd()
            )
            reservedCryptoView.text = getString(
                R.string.exchange_coin_to_coin_screen_balance_crypto,
                fromCoinItem.reservedBalanceCoin.toStringCoin(),
                fromCoinItem.code
            )
            reservedUsdView.text = getString(
                R.string.exchange_coin_to_coin_screen_balance_usd,
                fromCoinItem.reservedBalanceUsd.toStringUsd()
            )
        }
    }

    private fun createTrade() {
        when {
            isEmptyFields() -> showError(R.string.error_please_fill_all_fields)
            !isCorrectLimits() -> showError(R.string.add_edit_trade_screen_max_min_limit_error)
            !isCorrectMargin() -> showError(R.string.add_edit_trade_screen_margin_error)
            else -> viewModel.createTrade(
                buyButtonView.isChecked,
                paymentMethodView.getString(),
                marginView.getString().toInt(),
                minLimitView.getString().toLong(),
                maxLimitView.getString().toLong(),
                tradeTermsView.getString()
            )
        }
    }

    private fun isEmptyFields(): Boolean = paymentMethodView.getString().isEmpty()
            || marginView.getString().isEmpty()
            || minLimitView.getString().isEmpty()
            || maxLimitView.getString().isEmpty()
            || tradeTermsView.getString().isEmpty()

    private fun isCorrectLimits(): Boolean = minLimitView.getString().toInt() < maxLimitView.getString().toInt()

    private fun isCorrectMargin(): Boolean = marginView.getString().toInt() <= 100

    companion object {
        const val TAG_COIN_ITEM = "tag_coin_item"
    }
}