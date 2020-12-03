package com.app.belcobtm.presentation.features.deals.swap

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.domain.wallet.interactor.GetCoinDetailsUseCase
import com.app.belcobtm.domain.wallet.interactor.GetCoinListUseCase
import com.app.belcobtm.domain.wallet.item.CoinDataItem
import com.app.belcobtm.presentation.core.extensions.withScale
import com.app.belcobtm.presentation.core.mvvm.LoadingData

class SwapViewModel(
    getCoinListUseCase: GetCoinListUseCase,
    private val getCoinDetailsUseCase: GetCoinDetailsUseCase
) : ViewModel() {

    val originCoinsData = getCoinListUseCase()

    private val _coinToSend = MutableLiveData<CoinDataItem>()
    val coinToSend: LiveData<CoinDataItem> = _coinToSend

    private val _coinToReceive = MutableLiveData<CoinDataItem>()
    val coinToReceive: LiveData<CoinDataItem> = _coinToReceive

    private val _swapRate = MutableLiveData<SwapRateModelView>()
    val swapRate: LiveData<SwapRateModelView> = _swapRate

    private val _swapFee = MutableLiveData<SwapFeeModelView>()
    val swapFee: LiveData<SwapFeeModelView> = _swapFee

    private val _submitButtonEnabled = MutableLiveData<Boolean>(false)
    val submitButtonEnabled: LiveData<Boolean> = _submitButtonEnabled

    private val _coinsDetailsLoadingState = MutableLiveData<LoadingData<Unit>>()
    val coinsDetailsLoadingState: LiveData<LoadingData<Unit>> = _coinsDetailsLoadingState

    init {
        updateCoins(originCoinsData.first(), originCoinsData.last())
    }

    fun updateCoinToSend(coin: CoinDataItem) {
        if (coin != coinToSend.value) {
            updateCoins(coin, coinToReceive.value!!)
        }
    }

    fun updateCoinToReceive(coin: CoinDataItem) {
        if (coin != coinToReceive.value) {
            updateCoins(coinToSend.value!!, coin)
        }
    }

    private fun updateCoins(coinToSend: CoinDataItem, coinToReceive: CoinDataItem) {
        if (coinToSend == coinToReceive) {
            return
        }
        // notify UI that coin details is fetching
        _coinsDetailsLoadingState.value = LoadingData.Loading()
        getCoinDetailsUseCase(
            params = GetCoinDetailsUseCase.Params(coinToSend.code),
            onSuccess = { coinToSendDetails ->
                getCoinDetailsUseCase(
                    params = GetCoinDetailsUseCase.Params(coinToReceive.code),
                    onSuccess = { coinToReceiveDetails ->
                        val receiveAmount = 5
                        val platformFee = coinToReceiveDetails.txFee
                        val platformFeeCoinsAmount = receiveAmount * platformFee
                        val atomicAmount = 1 // probably will depend on the coin type
                        val atomicSwapAmount = calcSwapAmount(
                            coinToSend,
                            coinToReceive,
                            atomicAmount.toDouble()
                        ).withScale(coinToReceiveDetails.scale)
                        _coinToSend.value = coinToSend
                        _coinToReceive.value = coinToReceive
                        _swapRate.value = SwapRateModelView(
                            atomicAmount,
                            coinToSend.code,
                            atomicSwapAmount,
                            coinToReceive.code
                        )
                        _swapFee.value = SwapFeeModelView(
                            platformFee,
                            platformFeeCoinsAmount,
                            coinToReceive.code
                        )
                        // notify UI that coin details has beed successfully fetched
                        _coinsDetailsLoadingState.value = LoadingData.Success(Unit)
                    },
                    onError = { _coinsDetailsLoadingState.value = LoadingData.Error(it) }
                )
            },
            onError = { _coinsDetailsLoadingState.value = LoadingData.Error(it) }
        )
    }

    private fun calcSwapAmount(
        sendCoin: CoinDataItem,
        receiveCoin: CoinDataItem,
        sendCoinAmount: Double
    ): Double {
        val sendCoinPriceUSD = sendCoin.priceUsd
        val receiveCoinPriceUSD = receiveCoin.priceUsd
        return sendCoinPriceUSD * sendCoinAmount / receiveCoinPriceUSD
    }
}

data class SwapFeeModelView(
    val platformFeePercents: Double,
    val platformFeeCoinAmount: Double,
    val swapCoinCode: String
)

data class SwapRateModelView(
    val fromCoinAmount: Int,
    val fromCoinCode: String,
    val swapAmount: Double,
    val swapCoinCode: String
)
