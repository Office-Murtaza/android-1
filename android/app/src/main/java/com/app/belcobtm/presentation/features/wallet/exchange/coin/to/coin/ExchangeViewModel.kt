package com.app.belcobtm.presentation.features.wallet.exchange.coin.to.coin

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.domain.transaction.interactor.ExchangeUseCase
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.domain.wallet.item.CoinDataItem
import com.app.belcobtm.domain.wallet.item.CoinFeeDataItem
import com.app.belcobtm.presentation.core.mvvm.LoadingData

class ExchangeViewModel(
    private val exchangeUseCase: ExchangeUseCase,
    val fromCoinItem: CoinDataItem,
    val coinItemList: List<CoinDataItem>,
    val fromCoinFeeItem: CoinFeeDataItem,
    val coinFeeItemList: Map<String, CoinFeeDataItem>
) : ViewModel() {
    val exchangeLiveData: MutableLiveData<LoadingData<Unit>> = MutableLiveData()
    var toCoinItem: CoinDataItem? = coinItemList.find { it.code == LocalCoinType.BTC.name }
    var toCoinFeeItem: CoinFeeDataItem? = coinFeeItemList[LocalCoinType.BTC.name]

    fun exchange(fromCoinAmount: Double) {
        exchangeLiveData.value = LoadingData.Loading()
        exchangeUseCase.invoke(
            ExchangeUseCase.Params(fromCoinAmount, fromCoinItem.code, toCoinItem?.code ?: ""),
            onSuccess = { exchangeLiveData.value = LoadingData.Success(it) },
            onError = { exchangeLiveData.value = LoadingData.Error(it) }
        )
    }
}