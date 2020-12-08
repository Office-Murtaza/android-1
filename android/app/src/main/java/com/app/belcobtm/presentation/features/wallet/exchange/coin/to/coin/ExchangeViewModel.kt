package com.app.belcobtm.presentation.features.wallet.exchange.coin.to.coin

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.domain.transaction.interactor.ExchangeUseCase
import com.app.belcobtm.domain.wallet.interactor.GetCoinDetailsMapUseCase
import com.app.belcobtm.domain.wallet.interactor.UpdateCoinDetailsUseCase
import com.app.belcobtm.domain.wallet.item.CoinDataItem
import com.app.belcobtm.domain.wallet.item.CoinDetailsDataItem
import com.app.belcobtm.presentation.core.coin.AmountCoinValidator
import com.app.belcobtm.presentation.core.coin.CoinCodeProvider
import com.app.belcobtm.presentation.core.coin.MinMaxCoinValueProvider
import com.app.belcobtm.presentation.core.coin.model.ValidationResult
import com.app.belcobtm.presentation.core.extensions.withScale
import com.app.belcobtm.presentation.core.mvvm.LoadingData

class ExchangeViewModel(
    private val exchangeUseCase: ExchangeUseCase,
    private val getCoinDetailsUseCase: GetCoinDetailsMapUseCase,
    private val updateCoinDetailsUseCase: UpdateCoinDetailsUseCase,
    val fromCoinItem: CoinDataItem,
    val fromCoinDetailsItem: CoinDetailsDataItem,
    val toCoinItemList: List<CoinDataItem>,
    private val minMaxCoinValueProvider: MinMaxCoinValueProvider,
    private val coinCodeProvider: CoinCodeProvider,
    private val amountCoinValidator: AmountCoinValidator
) : ViewModel() {
    val exchangeLiveData: MutableLiveData<LoadingData<Unit>> = MutableLiveData()
    val coinDetailsLiveData: MutableLiveData<LoadingData<Unit>> = MutableLiveData()
    var toCoinItem: CoinDataItem? = null
        set(value) {
            if (field == value || value == null) {
                return
            }
            val coinCode = value.code
            val coinDetailsMap = getCoinDetailsUseCase.getCoinDetailsMap()
            if (coinDetailsMap[coinCode] == null) {
                val params = UpdateCoinDetailsUseCase.Params(coinCode)
                coinDetailsLiveData.value = LoadingData.Loading()
                updateCoinDetailsUseCase.invoke(
                    params = params,
                    onSuccess = {
                        field = value
                        coinDetailsLiveData.value = LoadingData.Success(Unit)
                    },
                    onError = { coinDetailsLiveData.value = LoadingData.Error(it) }
                )
            } else {
                field = value
            }
        }

    init {
        toCoinItem = toCoinItemList.firstOrNull { it.code != fromCoinItem.code }
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

    fun getCoinToAmount(fromCoinAmount: Double): Double {
        val toCoinAmount = fromCoinAmount * fromCoinItem.priceUsd / (toCoinItem?.priceUsd
            ?: 0.0) * (100 - fromCoinDetailsItem.profitExchange) / 100
        // try to get saved scale
        val coinDetailsMap = getCoinDetailsUseCase.getCoinDetailsMap()
        val currentCoinDetails = coinDetailsMap.getValue(toCoinItem!!.code)
        return toCoinAmount.withScale(currentCoinDetails.scale)
    }

    fun getFromMinValue(): Double =
        minMaxCoinValueProvider.getMinValue(fromCoinItem, fromCoinDetailsItem)

    fun getToMinValue(): Double {
        val coinDataItem = toCoinItem ?: return 0.0
        val coinDetails = getCoinDetailsUseCase.getCoinDetailsMap().getValue(coinDataItem.code)
        return minMaxCoinValueProvider.getMinValue(coinDataItem, coinDetails)
    }

    fun getMaxValue(): Double =
        minMaxCoinValueProvider.getMaxValue(fromCoinItem, fromCoinDetailsItem)

    fun getCoinCode(): String =
        coinCodeProvider.getCoinCode(fromCoinItem)

    fun validateAmount(amount: Double): ValidationResult =
        amountCoinValidator.validateBalance(
            amount, fromCoinItem, fromCoinDetailsItem, toCoinItemList
        )
}