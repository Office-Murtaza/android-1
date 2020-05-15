package com.app.belcobtm.presentation.features.wallet.trade

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.domain.wallet.interactor.GetTradeInfoUseCase
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.features.wallet.IntentCoinItem
import com.app.belcobtm.presentation.features.wallet.trade.item.TradePageItem
import com.app.belcobtm.presentation.features.wallet.trade.item.mapToUiBuyItem

class TradeViewModel(
    val fromCoinItem: IntentCoinItem,
    private val getTradeInfoUseCase: GetTradeInfoUseCase
) : ViewModel() {
    val tradePageListLiveData: MutableLiveData<LoadingData<List<TradePageItem>>> = MutableLiveData()

    init {
        tradePageListLiveData.value = LoadingData.Loading()
        getTradeInfoUseCase.invoke(Unit) { either ->
            either.either(
                { tradePageListLiveData.value = LoadingData.Error(it) },
                { tradeInfoItem ->
                    val pageList = mutableListOf<TradePageItem>()
                    val buyItem = TradePageItem(tradeInfoItem.buyTrades.map { it.mapToUiBuyItem() })
                    pageList.add(TRADE_POSITION_BUY, buyItem)

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