package com.app.belcobtm.presentation.features.wallet.trade.create

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.domain.wallet.interactor.trade.CreateBuyTradeUseCase
import com.app.belcobtm.domain.wallet.interactor.trade.CreateSellTradeUseCase
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.features.wallet.IntentCoinItem

class CreateTradeViewModel(
    val fromCoinItem: IntentCoinItem,
    private val createTradeBuyUseCase: CreateBuyTradeUseCase,
    private val createTradeSellUseCase: CreateSellTradeUseCase
) : ViewModel() {
    val createTradeLiveData: MutableLiveData<LoadingData<Unit>> = MutableLiveData()

    fun createTrade(
        isBuyChecked: Boolean,
        paymentMethod: String,
        margin: Int,
        minLimit: Long,
        maxLimit: Long,
        terms: String
    ) {
        createTradeLiveData.value = LoadingData.Loading()
        if (isBuyChecked) {
            createTradeBuyUseCase.invoke(
                params = CreateBuyTradeUseCase.Params(
                    fromCoinItem.coinCode,
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
                    fromCoinItem.coinCode,
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