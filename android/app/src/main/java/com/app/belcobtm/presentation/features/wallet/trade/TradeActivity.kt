package com.app.belcobtm.presentation.features.wallet.trade

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import com.app.belcobtm.R
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.presentation.core.extensions.toStringCoin
import com.app.belcobtm.presentation.core.extensions.toStringUsd
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.ui.BaseActivity
import com.app.belcobtm.presentation.features.authorization.pin.PinActivity
import com.app.belcobtm.presentation.features.wallet.trade.adapter.TradePageAdapter
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_trade.*
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class TradeActivity : BaseActivity() {
    private val viewModel: TradeViewModel by viewModel { parametersOf(intent.getParcelableExtra(TAG_COIN_ITEM)) }
    private val tradePageAdapter = TradePageAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trade)

        initListeners()
        initObservers()
        initViews()
    }

    private fun initListeners() {}

    private fun initObservers() {
        viewModel.tradePageListLiveData.observe(this, Observer { loadingData ->
            when (loadingData) {
                is LoadingData.Loading -> {
                }//progressView.show()
                is LoadingData.Success -> {
                    tradePageAdapter.setItemList(loadingData.data)
//                    progressView.hide()
                }
                is LoadingData.Error -> {
                    when (loadingData.errorType) {
                        is Failure.TokenError -> startActivity(Intent(this, PinActivity::class.java))
                        is Failure.MessageError -> showError(loadingData.errorType.message)
                        is Failure.NetworkConnection -> showError(R.string.error_internet_unavailable)
                        else -> showError(R.string.error_something_went_wrong)
                    }
//                    progressView.hide()
                }
            }
        })
    }

    private fun initViews() {
        setSupportActionBar(toolbarView)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.trade_screen_title, viewModel.fromCoinItem.coinCode)

        priceUsdView.text = getString(
            R.string.exchange_coin_to_coin_screen_price_value,
            viewModel.fromCoinItem.priceUsd.toStringUsd()
        )
        balanceCryptoView.text = getString(
            R.string.exchange_coin_to_coin_screen_balance_crypto,
            viewModel.fromCoinItem.balanceCoin.toStringCoin(),
            viewModel.fromCoinItem.coinCode
        )
        balanceUsdView.text = getString(
            R.string.exchange_coin_to_coin_screen_balance_usd,
            viewModel.fromCoinItem.balanceUsd.toStringUsd()
        )
        reserveCryptoView.text = getString(
            R.string.exchange_coin_to_coin_screen_balance_crypto,
            viewModel.fromCoinItem.reservedBalanceCoin.toStringCoin(),
            viewModel.fromCoinItem.coinCode
        )
        reserveUsdView.text = getString(
            R.string.exchange_coin_to_coin_screen_balance_usd,
            viewModel.fromCoinItem.reservedBalanceUsd.toStringUsd()
        )
        pagerView.adapter = tradePageAdapter
        TabLayoutMediator(tabLayoutView, pagerView) { tab, position ->
            tab.text = when (position) {
                TradeViewModel.TRADE_POSITION_BUY -> getString(R.string.trade_screen_tab_trades_buy)
                TradeViewModel.TRADE_POSITION_SELL -> getString(R.string.trade_screen_tab_trades_sell)
                TradeViewModel.TRADE_POSITION_OPEN -> getString(R.string.trade_screen_tab_trades_open)
                else -> ""
            }
        }.attach()
    }

    companion object {
        const val TAG_COIN_ITEM = "tag_coin_item"
    }
}