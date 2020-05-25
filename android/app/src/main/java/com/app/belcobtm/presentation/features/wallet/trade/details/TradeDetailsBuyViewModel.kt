package com.app.belcobtm.presentation.features.wallet.trade.details

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.domain.wallet.interactor.TradeBuyUseCase
import com.app.belcobtm.domain.wallet.item.CoinFeeDataItem
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.features.wallet.IntentCoinItem

class TradeDetailsBuyViewModel(
    val fromCoinItem: IntentCoinItem,
    private val buyUseCase: TradeBuyUseCase
) : ViewModel() {
    val buyLiveData: MutableLiveData<LoadingData<Unit>> = MutableLiveData()

    fun buy(id: Int, price: Double, fromUsdAmount: Int, toCoinAmount: Double, detailsText: String) {
        buyLiveData.value = LoadingData.Loading()
        buyUseCase.invoke(
            TradeBuyUseCase.Params(
                id,
                price.toInt(),
                fromUsdAmount,
                fromCoinItem.coinCode,
                toCoinAmount,
                detailsText
            )
        ) { either ->
            either.either(
                { buyLiveData.value = LoadingData.Error(it) },
                { buyLiveData.value = LoadingData.Success(it) }
            )
        }
    }
}