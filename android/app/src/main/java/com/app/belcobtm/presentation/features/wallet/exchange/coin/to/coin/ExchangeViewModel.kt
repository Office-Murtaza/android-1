package com.app.belcobtm.presentation.features.wallet.exchange.coin.to.coin

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.domain.transaction.interactor.ExchangeUseCase
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.domain.wallet.item.CoinDataItem
import com.app.belcobtm.domain.wallet.item.CoinFeeDataItem
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import kotlin.math.max

class ExchangeViewModel(
    private val exchangeUseCase: ExchangeUseCase,
    val fromCoinItem: CoinDataItem,
    val coinItemList: List<CoinDataItem>,
    val fromCoinFeeItem: CoinFeeDataItem
) : ViewModel() {
    val exchangeLiveData: MutableLiveData<LoadingData<Unit>> = MutableLiveData()
    var toCoinItem: CoinDataItem? = null

    init {
        toCoinItem = coinItemList.firstOrNull { it.code != fromCoinItem.code }
    }

    fun exchange(fromCoinAmount: Double) {
        val toCoinAmount: Double = getCoinToAmount(fromCoinAmount)
        exchangeLiveData.value = LoadingData.Loading()
        exchangeUseCase.invoke(
            params = ExchangeUseCase.Params(
                fromCoinAmount,
                toCoinAmount,
                fromCoinItem.code,
                toCoinItem?.code ?: ""
            ),
            onSuccess = { exchangeLiveData.value = LoadingData.Success(it) },
            onError = { exchangeLiveData.value = LoadingData.Error(it) }
        )
    }

    fun getCoinToAmount(fromCoinAmount: Double): Double =
        fromCoinAmount * fromCoinItem.priceUsd / (toCoinItem?.priceUsd
            ?: 0.0) * (100 - fromCoinFeeItem.profitExchange) / 100

    fun getMaxValue(): Double = when (fromCoinItem.code) {
        LocalCoinType.CATM.name -> fromCoinItem.balanceCoin
        LocalCoinType.XRP.name -> max(0.0, fromCoinItem.balanceCoin - fromCoinFeeItem.txFee - 20)
        else -> max(0.0, fromCoinItem.balanceCoin) - fromCoinFeeItem.txFee
    }
}