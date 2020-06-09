package com.app.belcobtm.presentation.features.wallet.trade.main

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.lifecycle.Observer
import com.app.belcobtm.R
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.wallet.type.TradeSortType
import com.app.belcobtm.presentation.core.extensions.toStringCoin
import com.app.belcobtm.presentation.core.extensions.toStringUsd
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.ui.BaseActivity
import com.app.belcobtm.presentation.features.authorization.pin.PinActivity
import com.app.belcobtm.presentation.features.wallet.IntentCoinItem
import com.app.belcobtm.presentation.features.wallet.trade.create.CreateTradeActivity
import com.app.belcobtm.presentation.features.wallet.trade.details.TradeDetailsBuyActivity
import com.app.belcobtm.presentation.features.wallet.trade.main.adapter.TradePageAdapter
import com.app.belcobtm.presentation.features.wallet.trade.main.item.TradeDetailsItem
import com.app.belcobtm.presentation.features.wallet.trade.main.type.TradeTabType
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_trade.*
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class TradeActivity : BaseActivity() {
    private val viewModel: TradeViewModel by viewModel {
        parametersOf(
            intent.getDoubleExtra(TAG_LATITUDE, 0.0),
            intent.getDoubleExtra(TAG_LONGITUDE, 0.0),
            intent.getParcelableExtra(TAG_COIN_ITEM)
        )
    }
    private val tradePageAdapter = TradePageAdapter { tradeListItem ->
        when (tradeListItem) {
//            is TradeDetailsItem.Open,
//            is TradeDetailsItem.My,
            is TradeDetailsItem.BuySell -> {
                val intent = Intent(this, TradeDetailsBuyActivity::class.java)
                intent.putExtra(
                    TradeDetailsBuyActivity.TAG_COIN_ITEM,
                    this.intent.getParcelableExtra<IntentCoinItem>(TAG_COIN_ITEM)
                )
                intent.putExtra(TradeDetailsBuyActivity.TAG_TRADE_DETAILS_ITEM, tradeListItem)
                startActivity(intent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trade)
        initListeners()
        initObservers()
        initViews()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initListeners() {
        fabMenuView.setOnMenuToggleListener {
            if (it) {
                fabMenuView.setOnClickListener { fabMenuView.close(true) }
                fabMenuView.isClickable = true
            } else {
                fabMenuView.setOnClickListener(null)
                fabMenuView.isClickable = false
                fabMenuView.isFocusable = false
            }
        }
        createButtonView.setOnClickListener {
            val intent = Intent(this, CreateTradeActivity::class.java)
            intent.putExtra(
                CreateTradeActivity.TAG_COIN_ITEM,
                this.intent.getParcelableExtra<IntentCoinItem>(TAG_COIN_ITEM)
            )
            startActivity(intent)
            fabMenuView.close(true)
        }
        recallButtonView.setOnClickListener { fabMenuView.close(true) }
        reverseButtonView.setOnClickListener { fabMenuView.close(true) }
        postButtonView.setOnClickListener { fabMenuView.close(true) }
        priceButtonView.setOnClickListener {
            tradePageAdapter.clearData()
            viewModel.updateSorting(TradeSortType.PRICE)
        }
        distanceButtonView.setOnClickListener {
            tradePageAdapter.clearData()
            viewModel.updateSorting(TradeSortType.DISTANCE)
        }
    }

    private fun initObservers() {
        viewModel.buyListLiveData.observe(this, Observer { loadingData ->
            when (loadingData) {
                is LoadingData.Loading -> {
                    val listSize = tradePageAdapter.itemList[TradeTabType.BUY.ordinal].size
                    if (listSize == 0) {
                        tradePageAdapter.setBuyList(listOf(TradeDetailsItem.Loading))
                    }
                }
                is LoadingData.Success -> tradePageAdapter.setBuyList(loadingData.data)
                is LoadingData.Error -> tradePageAdapter.setBuyList(listOf(TradeDetailsItem.Error))
            }
        })

        viewModel.sellListLiveData.observe(this, Observer { loadingData ->
            when (loadingData) {
                is LoadingData.Loading -> {
                    val listSize = tradePageAdapter.itemList[TradeTabType.SELL.ordinal].size
                    if (listSize == 0) {
                        tradePageAdapter.setSellList(listOf(TradeDetailsItem.Loading))
                    }
                }
                is LoadingData.Success -> tradePageAdapter.setSellList(loadingData.data)
                is LoadingData.Error -> tradePageAdapter.setSellList(listOf(TradeDetailsItem.Error))
            }
        })

        viewModel.myListLiveData.observe(this, Observer { loadingData ->
            when (loadingData) {
                is LoadingData.Loading -> {
                    val listSize = tradePageAdapter.itemList[TradeTabType.MY.ordinal].size
                    if (listSize == 0) {
                        tradePageAdapter.setMyList(listOf(TradeDetailsItem.Loading))
                    }
                }
                is LoadingData.Success -> tradePageAdapter.setMyList(loadingData.data)
                is LoadingData.Error -> tradePageAdapter.setMyList(listOf(TradeDetailsItem.Error))
            }
        })

        viewModel.openListLiveData.observe(this, Observer { loadingData ->
            when (loadingData) {
                is LoadingData.Loading -> {
                    val listSize = tradePageAdapter.itemList[TradeTabType.OPEN.ordinal].size
                    if (listSize == 0) {
                        tradePageAdapter.setOpenList(listOf(TradeDetailsItem.Loading))
                    }
                }
                is LoadingData.Success -> tradePageAdapter.setOpenList(loadingData.data)
                is LoadingData.Error -> tradePageAdapter.setOpenList(listOf(TradeDetailsItem.Error))
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
                TradeTabType.BUY.ordinal -> getString(R.string.trade_screen_tab_trades_buy)
                TradeTabType.SELL.ordinal -> getString(R.string.trade_screen_tab_trades_sell)
                TradeTabType.MY.ordinal -> getString(R.string.trade_screen_tab_trades_my)
                TradeTabType.OPEN.ordinal -> getString(R.string.trade_screen_tab_trades_open)
                else -> ""
            }
        }.attach()
    }

    private fun errorResponse(failure: Exception?) {
        when (failure) {
            is Failure.TokenError -> startActivity(Intent(this, PinActivity::class.java))
            is Failure.MessageError -> showError(failure.message)
            is Failure.NetworkConnection -> showError(R.string.error_internet_unavailable)
            else -> showError(R.string.error_something_went_wrong)
        }
    }

    companion object {
        const val TAG_COIN_ITEM = "tag_coin_item"
        const val TAG_LATITUDE = "tag_latitude"
        const val TAG_LONGITUDE = "tag_longitude"
    }
}