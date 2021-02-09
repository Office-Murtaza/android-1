package com.app.belcobtm.presentation.features.wallet.trade.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.domain.transaction.interactor.TradeBuySellUseCase
import com.app.belcobtm.domain.wallet.interactor.GetCoinByCodeUseCase
import com.app.belcobtm.domain.wallet.item.CoinDataItem
import com.app.belcobtm.presentation.core.mvvm.LoadingData

class TradeDetailsBuyViewModel(
    coinCode: String,
    getCoinByCodeUseCase: GetCoinByCodeUseCase,
    private val tradeBuySellUseCase: TradeBuySellUseCase
) : ViewModel() {

    private val _loadingData = MutableLiveData<LoadingData<Unit>>()
    val loadingData: LiveData<LoadingData<Unit>> = _loadingData

    val buyLiveData: MutableLiveData<LoadingData<Unit>> = MutableLiveData()

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