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

    private val _coinToSendError = MutableLiveData<ValidationError>(ValidationError.None)
    val coinToSendError: LiveData<ValidationError> = _coinToSendError

    private val _coinsDetailsLoadingState = MutableLiveData<LoadingData<Unit>>()
    val coinsDetailsLoadingState: LiveData<LoadingData<Unit>> = _coinsDetailsLoadingState

    private val _sendCoinAmount = MutableLiveData<Double>()
    val sendCoinAmount: LiveData<Double> = _sendCoinAmount

    private val _receiveCoinAmount = MutableLiveData<Double>()
    val receiveCoinAmount: LiveData<Double> = _receiveCoinAmount

    init {
        updateCoins(originCoinsData.first(), originCoinsData.last())
    }

    fun setCoinToSend(coin: CoinDataItem) {
        if (coin != coinToSend.value) {
            updateCoins(coin, coinToReceive.value!!)
        }
    }

    fun setCoinToReceive(coin: CoinDataItem) {
        if (coin != coinToReceive.value) {
            updateCoins(coinToSend.value!!, coin)
        }
    }

    fun setSendAmount(sendAmount: Double) {
        _sendCoinAmount.value = sendAmount
        _receiveCoinAmount.value = calcReceiveAmountFromSend(sendAmount)
        validateCoinToSendAmount(sendAmount)
    }

    fun setReceiveAmount(receiveAmount: Double) {
        _receiveCoinAmount.value = receiveAmount
        _sendCoinAmount.value = calcSendAmountFromReceive(receiveAmount)
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
                        val receiveAmount = receiveCoinAmount.value ?: 1.0
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
                        // recalc the data
                        val sendCoinAmountLocal = _sendCoinAmount.value
                        val receiveCoinAmountLocal = _receiveCoinAmount.value
                        if (sendCoinAmountLocal != null) {
                            setSendAmount(sendCoinAmountLocal)
                        } else if (receiveCoinAmountLocal != null) {
                            setReceiveAmount(receiveCoinAmountLocal)
                        }
                        // notify UI that coin details has beed successfully fetched
                        _coinsDetailsLoadingState.value = LoadingData.Success(Unit)
                    },
                    onError = { _coinsDetailsLoadingState.value = LoadingData.Error(it) }
                )
            },
            onError = { _coinsDetailsLoadingState.value = LoadingData.Error(it) }
        )
    }

    private fun validateCoinToSendAmount(coinAmount: Double) {
        val currentCoinToSend = coinToSend.value ?: return
        if (coinAmount > currentCoinToSend.balanceCoin) {
            _coinToSendError.value = ValidationError.AmountLessThanBalance
            return
        }
        _coinToSendError.value = ValidationError.None
    }

    private fun calcReceiveAmountFromSend(sendAmount: Double): Double {
        val currentCoinToSend = coinToSend.value ?: return 0.0
        val currentCoinToReceive = coinToReceive.value ?: return 0.0
        return calcSwapAmount(currentCoinToSend, currentCoinToReceive, sendAmount)
    }

    private fun calcSendAmountFromReceive(receiveAmount: Double): Double {
        val currentCoinToSend = coinToSend.value ?: return 0.0
        val currentCoinToReceive = coinToReceive.value ?: return 0.0
        return calcSwapAmount(currentCoinToReceive, currentCoinToSend, receiveAmount)
    }

    private fun calcSwapAmount(from: CoinDataItem, to: CoinDataItem, amount: Double): Double {
        val fromCoinPriceUSD = from.priceUsd
        val toCoinPriceUSD = to.priceUsd
        return fromCoinPriceUSD * amount / toCoinPriceUSD
    }
}

sealed class ValidationError {
    object None : ValidationError()
    object AmountLessThanBalance : ValidationError()
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
