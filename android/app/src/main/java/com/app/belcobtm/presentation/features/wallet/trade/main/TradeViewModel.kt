package com.app.belcobtm.presentation.features.wallet.trade.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.domain.transaction.interactor.trade.GetListTradeUseCase
import com.app.belcobtm.domain.transaction.type.TradeSortType
import com.app.belcobtm.domain.wallet.interactor.GetFreshCoinUseCase
import com.app.belcobtm.domain.wallet.item.CoinDataItem
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.features.wallet.trade.main.item.TradeDetailsItem
import com.app.belcobtm.presentation.features.wallet.trade.main.item.mapToUiBuySellItem
import com.app.belcobtm.presentation.features.wallet.trade.main.item.mapToUiMyItem
import com.app.belcobtm.presentation.features.wallet.trade.main.item.mapToUiOpenItem

class TradeViewModel(
    private val coinCode: String,
    private val latitude: Double,
    private val longitude: Double,
    private val freshCoinUseCase: GetFreshCoinUseCase,
    private val getBuyListUseCase: GetListTradeUseCase.Buy,
    private val getSellListUseCase: GetListTradeUseCase.Sell,
    private val getMyListUseCase: GetListTradeUseCase.My,
    private val getOpenListUseCase: GetListTradeUseCase.Open
) : ViewModel() {
    private var sortType: TradeSortType = TradeSortType.PRICE
    val fromCoinLiveData: MutableLiveData<LoadingData<CoinDataItem>> = MutableLiveData()
    val buyListLiveData: MutableLiveData<LoadingData<Pair<Int, List<TradeDetailsItem.BuySell>>>> = MutableLiveData()
    val sellListLiveData: MutableLiveData<LoadingData<Pair<Int, List<TradeDetailsItem.BuySell>>>> = MutableLiveData()
    val myListLiveData: MutableLiveData<LoadingData<Pair<Int, List<TradeDetailsItem.My>>>> = MutableLiveData()
    val openListLiveData: MutableLiveData<LoadingData<Pair<Int, List<TradeDetailsItem.Open>>>> = MutableLiveData()

    fun updateDataItem() {
        fromCoinLiveData.value = LoadingData.Loading()
        freshCoinUseCase.invoke(params = GetFreshCoinUseCase.Params(coinCode),
            onSuccess = { fromCoinLiveData.value = LoadingData.Success(it) },
            onError = { fromCoinLiveData.value = LoadingData.Error(it) }
        )
    }

    fun updateSorting(sortType: TradeSortType?) {
        this.sortType = sortType ?: this.sortType
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
                val maxSize = dataItem.total
                val list = dataItem.tradeList.map { it.mapToUiBuySellItem(true) }
                buyListLiveData.value = LoadingData.Success(maxSize to list)
            },
            onError = { buyListLiveData.value = LoadingData.Error(it) }
        )
    }

    fun updateSellList(paginationStep: Int) {
        sellListLiveData.value = LoadingData.Loading()
        getSellListUseCase.invoke(
            createGetTradeListParams(paginationStep),
            onSuccess = { dataItem ->
                val maxSize = dataItem.total
                val list = dataItem.tradeList.map { it.mapToUiBuySellItem(true) }
                sellListLiveData.value = LoadingData.Success(maxSize to list)
            },
            onError = { sellListLiveData.value = LoadingData.Error(it) }
        )
    }

    fun updateMyList(paginationStep: Int) {
        myListLiveData.value = LoadingData.Loading()
        getMyListUseCase.invoke(
            createGetTradeListParams(paginationStep),
            onSuccess = { dataItem ->
                val maxSize = dataItem.total
                val list = dataItem.tradeList.map { it.mapToUiMyItem() }
                myListLiveData.value = LoadingData.Success(maxSize to list)
            },
            onError = { myListLiveData.value = LoadingData.Error(it) }
        )
    }

    fun updateOpenList(paginationStep: Int) {
        openListLiveData.value = LoadingData.Loading()
        getOpenListUseCase.invoke(
            createGetTradeListParams(paginationStep),
            onSuccess = { dataItem ->
                val maxSize = dataItem.total
                val list = dataItem.tradeList.map { it.mapToUiOpenItem() }
                openListLiveData.value = LoadingData.Success(maxSize to list)
            },
            onError = { openListLiveData.value = LoadingData.Error(it) }
        )
    }

    private fun createGetTradeListParams(paginationStep: Int) =
        GetListTradeUseCase.Params(latitude, longitude, coinCode, sortType, paginationStep)
}