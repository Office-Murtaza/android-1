package com.app.belcobtm.presentation.features.wallet.trade.create

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.domain.transaction.interactor.trade.CreateBuyTradeUseCase
import com.app.belcobtm.domain.transaction.interactor.trade.CreateSellTradeUseCase
import com.app.belcobtm.domain.wallet.interactor.GetCoinByCodeUseCase
import com.app.belcobtm.domain.wallet.item.CoinDataItem
import com.app.belcobtm.presentation.core.mvvm.LoadingData

class TradeCreateViewModel(
    coinCode: String,
    getCoinByCodeUseCase: GetCoinByCodeUseCase,
    private val createTradeBuyUseCase: CreateBuyTradeUseCase,
    private val createTradeSellUseCase: CreateSellTradeUseCase
) : ViewModel() {
    val createTradeLiveData: MutableLiveData<LoadingData<Unit>> = MutableLiveData()

    private val _loadingData = MutableLiveData<LoadingData<Unit>>()
    val loadingData: LiveData<LoadingData<Unit>> = _loadingData

    lateinit var fromCoinItem: CoinDataItem
        private set

    init {
        _loadingData.value = LoadingData.Loading()
        getCoinByCodeUseCase.invoke(coinCode, onSuccess = {
            fromCoinItem = it
            _loadingData.value = LoadingData.Success(Unit)
        }, onError = {
            _loadingData.value = LoadingData.Error(it)
        })
    }

    fun createTrade(
        isBuyChecked: Boolean,
        paymentMethod: String,
        margin: Double,
        minLimit: Long,
        maxLimit: Long,
        terms: String
    ) {
        createTradeLiveData.value = LoadingData.Loading()
        if (isBuyChecked) {
            createTradeBuyUseCase.invoke(
                params = CreateBuyTradeUseCase.Params(
                    fromCoinItem.code,
                    paymentMethod,
                    margin,
                    minLimit,
                    maxLimit,
                    terms
                ),
                onSuccess = { createTradeLiveData.value = LoadingData.Success(it) },
                onError = { createTradeLiveData.value = LoadingData.Error(it) }
            )
        } else {
            createTradeSellUseCase.invoke(
                params = CreateSellTradeUseCase.Params(
                    fromCoinItem.code,
                    paymentMethod,
                    margin,
                    minLimit,
                    maxLimit,
                    terms
                ),
                onSuccess = { createTradeLiveData.value = LoadingData.Success(it) },
                onError = { createTradeLiveData.value = LoadingData.Error(it) }
            )
        }
    }
}