package com.app.belcobtm.presentation.features.deals.swap

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.R
import com.app.belcobtm.domain.transaction.interactor.SwapUseCase
import com.app.belcobtm.domain.wallet.interactor.GetCoinDetailsUseCase
import com.app.belcobtm.domain.wallet.interactor.GetCoinListUseCase
import com.app.belcobtm.domain.wallet.item.CoinDataItem
import com.app.belcobtm.domain.wallet.item.CoinDetailsDataItem
import com.app.belcobtm.presentation.core.coin.AmountCoinValidator
import com.app.belcobtm.presentation.core.coin.MinMaxCoinValueProvider
import com.app.belcobtm.presentation.core.coin.model.ValidationResult
import com.app.belcobtm.presentation.core.mvvm.LoadingData

class SwapViewModel(
    getCoinListUseCase: GetCoinListUseCase,
    private val amountValidator: AmountCoinValidator,
    private val swapUseCase: SwapUseCase,
    private val minMaxCoinValueProvider: MinMaxCoinValueProvider,
    private val getCoinDetailsUseCase: GetCoinDetailsUseCase
) : ViewModel() {

    val originCoinsData = getCoinListUseCase()

    private var coinToSendDetails: CoinDetailsDataItem? = null
    private var coinToReceiveDetails: CoinDetailsDataItem? = null

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

    private val _coinToSendError = MutableLiveData<ValidationResult>(ValidationResult.Valid)
    val coinToSendError: LiveData<ValidationResult> = _coinToSendError

    private val _coinsDetailsLoadingState = MutableLiveData<LoadingData<Unit>>()
    val coinsDetailsLoadingState: LiveData<LoadingData<Unit>> = _coinsDetailsLoadingState

    private val _sendCoinAmount = MutableLiveData<Double>()
    val sendCoinAmount: LiveData<Double> = _sendCoinAmount

    private val _receiveCoinAmount = MutableLiveData<Double>()
    val receiveCoinAmount: LiveData<Double> = _receiveCoinAmount

    private val _swapLoadingData = MutableLiveData<LoadingData<Unit>>()
    val swapLoadingData: LiveData<LoadingData<Unit>> = _swapLoadingData

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
        // extra validation step
        validateCoinToSendAmount(sendAmount)
    }

    fun setReceiveAmount(receiveAmount: Double) {
        _receiveCoinAmount.value = receiveAmount
        _sendCoinAmount.value = calcSendAmountFromReceive(receiveAmount)
    }

    fun setMaxSendAmount() {
        val currentCoinToSend = coinToSend.value ?: return
        val currentCoinToSendDetails = coinToSendDetails ?: return
        val maxAmount = minMaxCoinValueProvider
            .getMaxValue(currentCoinToSend, currentCoinToSendDetails)
        setSendAmount(maxAmount)
    }

    fun executeSwap() {
        val sendCoinItem = coinToSend.value ?: return
        val receiveCoinItem = coinToReceive.value ?: return
        val sendCoinAmount = sendCoinAmount.value ?: return
        val receiveCoinAmount = receiveCoinAmount.value ?: return
        _swapLoadingData.value = LoadingData.Loading()
        swapUseCase(
            params = SwapUseCase.Params(
                sendCoinAmount,
                receiveCoinAmount,
                sendCoinItem.code,
                receiveCoinItem.code
            ),
            onSuccess = { _swapLoadingData.value = LoadingData.Success(it) },
            onError = { _swapLoadingData.value = LoadingData.Error(it) }
        )
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
                            coinToSendDetails,
                            coinToReceive,
                            coinToReceiveDetails,
                            atomicAmount.toDouble()
                        )
                        this.coinToSendDetails = coinToSendDetails
                        this.coinToReceiveDetails = coinToReceiveDetails
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
        val currentCoinToSendDetails = coinToSendDetails ?: return
        val balanceValidationResult = amountValidator.validateBalance(
            coinAmount, currentCoinToSend, currentCoinToSendDetails, originCoinsData
        )
        val minCoinAmount = minMaxCoinValueProvider
            .getMinValue(currentCoinToSend, currentCoinToSendDetails)
        val maxCoinAmount = minMaxCoinValueProvider
            .getMaxValue(currentCoinToSend, currentCoinToSendDetails)
        val validationResult = when {
            balanceValidationResult is ValidationResult.InValid -> {
                balanceValidationResult
            }
            coinAmount > maxCoinAmount -> {
                ValidationResult.InValid(R.string.swap_screen_max_error)
            }
            coinAmount < minCoinAmount -> {
                ValidationResult.InValid(R.string.swap_screen_min_error)
            }
            else -> {
                ValidationResult.Valid
            }
        }
        _coinToSendError.value = validationResult
        _submitButtonEnabled.value = validationResult == ValidationResult.Valid
    }

    private fun calcReceiveAmountFromSend(sendAmount: Double): Double {
        val currentCoinToSend = coinToSend.value ?: return 0.0
        val currentCoinToReceive = coinToReceive.value ?: return 0.0
        val currentCoinToSendDetails = coinToSendDetails ?: return 0.0
        val currentCoinToReceiveDetails = coinToReceiveDetails ?: return 0.0
        return calcSwapAmount(
            currentCoinToSend,
            currentCoinToSendDetails,
            currentCoinToReceive,
            currentCoinToReceiveDetails,
            sendAmount
        )
    }

    private fun calcSendAmountFromReceive(receiveAmount: Double): Double {
        val currentCoinToSend = coinToSend.value ?: return 0.0
        val currentCoinToReceive = coinToReceive.value ?: return 0.0
        val currentCoinToSendDetails = coinToSendDetails ?: return 0.0
        val currentCoinToReceiveDetails = coinToReceiveDetails ?: return 0.0
        return calcSwapAmount(
            currentCoinToReceive,
            currentCoinToReceiveDetails,
            currentCoinToSend,
            currentCoinToSendDetails,
            receiveAmount
        )
    }

    private fun calcSwapAmount(
        from: CoinDataItem,
        fromDetails: CoinDetailsDataItem,
        to: CoinDataItem,
        toDetails: CoinDetailsDataItem,
        amount: Double
    ): Double {
        val price = amount * from.priceUsd / to.priceUsd
        val result = price * (1 - fromDetails.profitExchange / 100) - toDetails.txFee
        return result
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
