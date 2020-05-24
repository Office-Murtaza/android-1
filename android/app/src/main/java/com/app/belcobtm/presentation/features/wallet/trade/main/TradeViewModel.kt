package com.app.belcobtm.presentation.features.wallet.trade.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.domain.wallet.interactor.GetTradeInfoUseCase
import com.app.belcobtm.domain.wallet.item.TradeInfoDataItem
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.features.wallet.IntentCoinItem
import com.app.belcobtm.presentation.features.wallet.trade.main.item.TradePageItem
import com.app.belcobtm.presentation.features.wallet.trade.main.item.mapToUiBuyItem
import com.app.belcobtm.presentation.features.wallet.trade.main.item.mapToUiOpenItem
import com.app.belcobtm.presentation.features.wallet.trade.main.item.mapToUiSellItem

class TradeViewModel(
    private val latitude: Double,
    private val longitude: Double,
    val fromCoinItem: IntentCoinItem,
    private val getTradeInfoUseCase: GetTradeInfoUseCase
) : ViewModel() {
    val tradePageListLiveData: MutableLiveData<LoadingData<List<TradePageItem>>> = MutableLiveData()

    init {
        tradePageListLiveData.value = LoadingData.Loading()
        getTradeInfoUseCase.invoke(GetTradeInfoUseCase.Params(latitude, longitude, fromCoinItem.coinCode)) { either ->
            either.either(
                { tradePageListLiveData.value = LoadingData.Error(it) },
                { tradeInfoItem ->
                    val pageList = mutableListOf<TradePageItem>()
                    val buyItems = TradePageItem(tradeInfoItem.buyTrades.map { it.mapToUiBuyItem() })
                    val sellItems = TradePageItem(tradeInfoItem.sellTrades.map { it.mapToUiSellItem() })
                    val openItems = TradePageItem(tradeInfoItem.openTrades.map { it.mapToUiOpenItem() })
                    pageList.add(TRADE_POSITION_BUY, buyItems)
                    pageList.add(TRADE_POSITION_SELL, sellItems)
                    pageList.add(TRADE_POSITION_OPEN, openItems)

                    tradePageListLiveData.value = LoadingData.Success(pageList)
                }
            )
        }
    }

    companion object {
        const val TRADE_POSITION_BUY = 0
        const val TRADE_POSITION_SELL = 1
        const val TRADE_POSITION_OPEN = 2
    }
}