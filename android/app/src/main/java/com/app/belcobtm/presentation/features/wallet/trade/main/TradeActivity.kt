package com.app.belcobtm.presentation.features.wallet.trade.main

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.MotionEvent
import androidx.lifecycle.Observer
import com.app.belcobtm.R
import com.app.belcobtm.databinding.ActivityTradeBinding
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.transaction.type.TradeSortType
import com.app.belcobtm.presentation.core.extensions.hide
import com.app.belcobtm.presentation.core.extensions.show
import com.app.belcobtm.presentation.core.extensions.toStringCoin
import com.app.belcobtm.presentation.core.extensions.toStringUsd
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.ui.BaseActivity
import com.app.belcobtm.presentation.core.views.InterceptableFrameLayout
import com.app.belcobtm.presentation.features.HostActivity
import com.app.belcobtm.presentation.features.wallet.trade.create.TradeCreateActivity
import com.app.belcobtm.presentation.features.wallet.trade.details.TradeDetailsBuyActivity
import com.app.belcobtm.presentation.features.wallet.trade.main.adapter.TradePageAdapter
import com.app.belcobtm.presentation.features.wallet.trade.main.item.TradeDetailsBuySellItem
import com.app.belcobtm.presentation.features.wallet.trade.main.item.TradeDetailsError
import com.app.belcobtm.presentation.features.wallet.trade.main.item.TradeDetailsLoading
import com.app.belcobtm.presentation.features.wallet.trade.main.type.TradeTabType
import com.google.android.material.tabs.TabLayoutMediator
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class TradeActivity : BaseActivity(), InterceptableFrameLayout.OnInterceptEventListener {

    private lateinit var binding: ActivityTradeBinding

    private val viewModel: TradeViewModel by viewModel {
        parametersOf(
            intent.getDoubleExtra(TAG_LATITUDE, 0.0),
            intent.getDoubleExtra(TAG_LONGITUDE, 0.0),
            intent.getStringExtra(TAG_COIN_CODE)
        )
    }
    private val tradePageAdapter = TradePageAdapter({ tradeListItem ->
        when (tradeListItem) {
//            is TradeDetailsOpenItem,
//            is TradeDetailsMyItem,
            is TradeDetailsBuySellItem -> {
                val intent = Intent(this, TradeDetailsBuyActivity::class.java)
                intent.putExtra(
                    TradeDetailsBuyActivity.TAG_COIN_CODE,
                    this.intent.getStringExtra(TAG_COIN_CODE)
                )
                intent.putExtra(TradeDetailsBuyActivity.TAG_TRADE_DETAILS_ITEM, tradeListItem)
                startActivity(intent)
            }
        }
    }, { tabIndex: Int, currentListSize: Int ->
        when (TradeTabType.values()[tabIndex]) {
            TradeTabType.BUY -> {
                val loadingData = viewModel.buyListLiveData.value
                if (loadingData is LoadingData.Success && loadingData.data.first > currentListSize) {
                    viewModel.updateBuyList(loadingData.data.second.size + 1)
                }
            }
            TradeTabType.SELL -> {
                val loadingData = viewModel.sellListLiveData.value
                if (loadingData is LoadingData.Success && loadingData.data.first > currentListSize) {
                    viewModel.updateSellList(loadingData.data.second.size + 1)
                }
            }
            TradeTabType.MY -> {
                val loadingData = viewModel.myListLiveData.value
                if (loadingData is LoadingData.Success && loadingData.data.first > currentListSize) {
                    viewModel.updateMyList(loadingData.data.second.size + 1)
                }
            }
            TradeTabType.OPEN -> {
                val loadingData = viewModel.openListLiveData.value
                if (loadingData is LoadingData.Success && loadingData.data.first > currentListSize) {
                    viewModel.updateOpenList(loadingData.data.second.size + 1)
                }
            }
        }
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTradeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.initListeners()
        binding.initObservers()
        binding.initViews()
    }

    override fun onResume() {
        super.onResume()
        tradePageAdapter.clearData()
        viewModel.updateSorting(null)
        viewModel.updateDataItem()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onIntercented(ev: MotionEvent) {
        if (ev.action == MotionEvent.ACTION_DOWN) hideSoftKeyboard()
    }

    private fun ActivityTradeBinding.initListeners() {
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
            val intent = Intent(this@TradeActivity, TradeCreateActivity::class.java)
            intent.putExtra(TradeCreateActivity.TAG_COIN_CODE, this@TradeActivity.intent.getStringExtra(TAG_COIN_CODE))
            startActivity(intent)
            fabMenuView.close(true)
        }
        priceButtonView.setOnClickListener {
            tradePageAdapter.clearData()
            viewModel.updateSorting(TradeSortType.PRICE)
        }
        distanceButtonView.setOnClickListener {
            tradePageAdapter.clearData()
            viewModel.updateSorting(TradeSortType.DISTANCE)
        }
        container.interceptListner = this@TradeActivity
    }

    private fun ActivityTradeBinding.initObservers() {
        viewModel.buyListLiveData.observe(this@TradeActivity, Observer { loadingData ->
            when (loadingData) {
                is LoadingData.Loading -> {
                    val listSize = tradePageAdapter.adapterList[TradeTabType.BUY.ordinal].content.size
                    if (listSize == 0) {
                        tradePageAdapter.setBuyList(listOf(TradeDetailsLoading()))
                    }
                }
                is LoadingData.Success -> tradePageAdapter.setBuyList(loadingData.data.second)
                is LoadingData.Error -> tradePageAdapter.setBuyList(listOf(TradeDetailsError()))
            }
        })

        viewModel.sellListLiveData.observe(this@TradeActivity, Observer { loadingData ->
            when (loadingData) {
                is LoadingData.Loading -> {
                    val listSize = tradePageAdapter.adapterList[TradeTabType.SELL.ordinal].content.size
                    if (listSize == 0) {
                        tradePageAdapter.setSellList(listOf(TradeDetailsLoading()))
                    }
                }
                is LoadingData.Success -> tradePageAdapter.setSellList(loadingData.data.second)
                is LoadingData.Error -> tradePageAdapter.setSellList(listOf(TradeDetailsError()))
            }
        })

        viewModel.myListLiveData.observe(this@TradeActivity, Observer { loadingData ->
            when (loadingData) {
                is LoadingData.Loading -> {
                    val listSize = tradePageAdapter.adapterList[TradeTabType.MY.ordinal].content.size
                    if (listSize == 0) {
                        tradePageAdapter.setMyList(listOf(TradeDetailsLoading()))
                    }
                }
                is LoadingData.Success -> tradePageAdapter.setMyList(loadingData.data.second)
                is LoadingData.Error -> tradePageAdapter.setMyList(listOf(TradeDetailsError()))
            }
        })

        viewModel.openListLiveData.observe(this@TradeActivity, Observer { loadingData ->
            when (loadingData) {
                is LoadingData.Loading -> {
                    val listSize = tradePageAdapter.adapterList[TradeTabType.OPEN.ordinal].content.size
                    if (listSize == 0) {
                        tradePageAdapter.setOpenList(listOf(TradeDetailsLoading()))
                    }
                }
                is LoadingData.Success -> tradePageAdapter.setOpenList(loadingData.data.second)
                is LoadingData.Error -> tradePageAdapter.setOpenList(listOf(TradeDetailsError()))
            }
        })

        viewModel.fromCoinLiveData.observe(this@TradeActivity, Observer { loadingData ->
            when (loadingData) {
                is LoadingData.Loading -> progressView.show()
                is LoadingData.Success -> with(loadingData.data) {
                    priceUsdView.text = getString(
                        R.string.text_usd,
                        priceUsd.toStringUsd()
                    )
                    balanceCryptoView.text = getString(
                        R.string.text_text,
                        balanceCoin.toStringCoin(),
                        code
                    )
                    balanceUsdView.text = getString(
                        R.string.text_usd,
                        balanceUsd.toStringUsd()
                    )
                    reserveCryptoView.text = getString(
                        R.string.text_text,
                        reservedBalanceCoin.toStringCoin(),
                        code
                    )
                    reserveUsdView.text = getString(
                        R.string.text_usd,
                        reservedBalanceUsd.toStringUsd()
                    )
                    progressView.hide()
                }
                is LoadingData.Error -> {
                    errorResponse(loadingData.errorType)
                    progressView.hide()
                }
            }
        })
    }

    private fun ActivityTradeBinding.initViews() {
        setSupportActionBar(toolbarView)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.trade_screen_title, intent.getStringExtra(TAG_COIN_CODE))
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
            is Failure.TokenError -> {
                val intent = Intent(this, HostActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            }
            is Failure.MessageError -> showError(failure.message)
            is Failure.NetworkConnection -> showError(R.string.error_internet_unavailable)
            else -> showError(R.string.error_something_went_wrong)
        }
    }

    companion object {
        const val TAG_COIN_CODE = "tag_coin_code"
        const val TAG_LATITUDE = "tag_latitude"
        const val TAG_LONGITUDE = "tag_longitude"
    }
}