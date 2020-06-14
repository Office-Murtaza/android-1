package com.app.belcobtm.presentation.features.wallet.trade.details

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.domain.transaction.interactor.TradeBuySellUseCase
import com.app.belcobtm.domain.wallet.item.CoinDataItem
import com.app.belcobtm.presentation.core.mvvm.LoadingData

class TradeDetailsBuyViewModel(
    val fromCoinItem: CoinDataItem,
    private val tradeBuySellUseCase: TradeBuySellUseCase
) : ViewModel() {
    val buyLiveData: MutableLiveData<LoadingData<Unit>> = MutableLiveData()

    fun buy(id: Int, price: Double, fromUsdAmount: Int, toCoinAmount: Double, detailsText: String) {
        buyLiveData.value = LoadingData.Loading()
        tradeBuySellUseCase.invoke(
            TradeBuySellUseCase.Params(
                id,
                price.toInt(),
                fromUsdAmount,
                fromCoinItem.code,
                toCoinAmount,
                detailsText
            ),
            onSuccess = { buyLiveData.value = LoadingData.Success(it) },
            onError = { buyLiveData.value = LoadingData.Error(it) }
        )
    }
}