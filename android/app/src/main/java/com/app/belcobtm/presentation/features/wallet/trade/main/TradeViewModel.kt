package com.app.belcobtm.presentation.features.wallet.trade.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.domain.wallet.interactor.trade.GetListTradeUseCase
import com.app.belcobtm.domain.wallet.type.TradeSortType
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.features.wallet.IntentCoinItem
import com.app.belcobtm.presentation.features.wallet.trade.main.item.TradeDetailsItem
import com.app.belcobtm.presentation.features.wallet.trade.main.item.mapToUiBuySellItem
import com.app.belcobtm.presentation.features.wallet.trade.main.item.mapToUiMyItem
import com.app.belcobtm.presentation.features.wallet.trade.main.item.mapToUiOpenItem

class TradeViewModel(
    private val latitude: Double,
    private val longitude: Double,
    val fromCoinItem: IntentCoinItem,
    private val getBuyListUseCase: GetListTradeUseCase.Buy,
    private val getSellListUseCase: GetListTradeUseCase.Sell,
    private val getMyListUseCase: GetListTradeUseCase.My,
    private val getOpenListUseCase: GetListTradeUseCase.Open
) : ViewModel() {
    private var sortType: TradeSortType = TradeSortType.PRICE

    val buyListLiveData: MutableLiveData<LoadingData<List<TradeDetailsItem.BuySell>>> = MutableLiveData()
    val sellListLiveData: MutableLiveData<LoadingData<List<TradeDetailsItem.BuySell>>> = MutableLiveData()
    val myListLiveData: MutableLiveData<LoadingData<List<TradeDetailsItem.My>>> = MutableLiveData()
    val openListLiveData: MutableLiveData<LoadingData<List<TradeDetailsItem.Open>>> = MutableLiveData()

    init {
        updateSorting(TradeSortType.PRICE)
    }

    fun updateSorting(sortType: TradeSortType) {
        this.sortType = sortType
        updateBuyList(1)
        updateSellList(1)
        updateMyList(1)
        updateOpenList(1)
    }

    fun updateBuyList(paginationStep: Int) {
        buyListLiveData.value = LoadingData.Loading()
        getBuyListUseCase.invoke(
            createGetTradeListParams(paginationStep),
            onSuccess = { dataItem ->
                buyListLiveData.value = LoadingData.Success(dataItem.tradeList.map { it.mapToUiBuySellItem(true) })
            },
            onError = { buyListLiveData.value = LoadingData.Error(it) }
        )
    }

    fun updateSellList(paginationStep: Int) {
        sellListLiveData.value = LoadingData.Loading()
        getSellListUseCase.invoke(
            createGetTradeListParams(paginationStep),
            onSuccess = { dataItem ->
                sellListLiveData.value = LoadingData.Success(dataItem.tradeList.map { it.mapToUiBuySellItem(false) })
            },
            onError = { sellListLiveData.value = LoadingData.Error(it) }
        )
    }

    fun updateMyList(paginationStep: Int) {
        myListLiveData.value = LoadingData.Loading()
        getMyListUseCase.invoke(
            createGetTradeListParams(paginationStep),
            onSuccess = { dataItem ->
                myListLiveData.value = LoadingData.Success(dataItem.tradeList.map { it.mapToUiMyItem() })
            },
            onError = { myListLiveData.value = LoadingData.Error(it) }
        )
    }

    fun updateOpenList(paginationStep: Int) {
        openListLiveData.value = LoadingData.Loading()
        getOpenListUseCase.invoke(
            createGetTradeListParams(paginationStep),
            onSuccess = { dataItem ->
                openListLiveData.value = LoadingData.Success(dataItem.tradeList.map { it.mapToUiOpenItem() })
            },
            onError = { openListLiveData.value = LoadingData.Error(it) }
        )
    }

    private fun createGetTradeListParams(paginationStep: Int) =
        GetListTradeUseCase.Params(latitude, longitude, fromCoinItem.coinCode, sortType, paginationStep)
}