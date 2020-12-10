package com.app.belcobtm.presentation.features.deals.swap

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.belcobtm.R
import com.app.belcobtm.data.websockets.wallet.WalletObserver
import com.app.belcobtm.data.websockets.wallet.model.WalletBalance
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.transaction.interactor.CheckXRPAddressActivatedUseCase
import com.app.belcobtm.domain.transaction.interactor.SwapUseCase
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.domain.wallet.interactor.GetCoinDetailsUseCase
import com.app.belcobtm.domain.wallet.item.CoinDataItem
import com.app.belcobtm.domain.wallet.item.CoinDetailsDataItem
import com.app.belcobtm.domain.wallet.item.isEthRelatedCoin
import com.app.belcobtm.presentation.core.coin.AmountCoinValidator
import com.app.belcobtm.presentation.core.coin.MinMaxCoinValueProvider
import com.app.belcobtm.presentation.core.coin.model.ValidationResult
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class SwapViewModel(
    private val walletObserver: WalletObserver,
    private val amountValidator: AmountCoinValidator,
    private val swapUseCase: SwapUseCase,
    private val checkXRPAddressActivatedUseCase: CheckXRPAddressActivatedUseCase,
    private val minMaxCoinValueProvider: MinMaxCoinValueProvider,
    private val getCoinDetailsUseCase: GetCoinDetailsUseCase
) : ViewModel() {

    val originCoinsData = mutableListOf<CoinDataItem>()

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

    private val _initLoadingData = MutableLiveData<LoadingData<Unit>>()
    val initLoadingData: LiveData<LoadingData<Unit>> = _initLoadingData

    init {
       initWalletObservation()
    }

    fun reconnectToWallet() {
        viewModelScope.launch {
            walletObserver.connect()
            initWalletObservation()
        }
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
        val sendAmount = calcSendAmountFromReceive(receiveAmount)
        _receiveCoinAmount.value = receiveAmount
        _sendCoinAmount.value = sendAmount
        // extra validation step
        validateCoinToSendAmount(sendAmount)
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
        val receiveCoinDetails = coinToReceiveDetails ?: return
        if (receiveCoinItem.code == LocalCoinType.XRP.name) {
            val minXRPValue = 20 + receiveCoinDetails.txFee
            if (receiveCoinAmount < minXRPValue) {
                _swapLoadingData.value = LoadingData.Loading()
                checkXRPAddressActivatedUseCase(
                    params = CheckXRPAddressActivatedUseCase.Param(receiveCoinDetails.walletAddress),
                    onSuccess = { addressActivated ->
                        if (addressActivated) {
                            executeSwapInternal(
                                sendCoinAmount,
                                receiveCoinAmount,
                                sendCoinItem,
                                receiveCoinItem
                            )
                        } else {
                            _swapLoadingData.value = LoadingData.Error(Failure.XRPLowAmountToSend)
                        }
                    },
                    onError = { _swapLoadingData.value = LoadingData.Error(it) }
                )
            }
        } else {
            executeSwapInternal(sendCoinAmount, receiveCoinAmount, sendCoinItem, receiveCoinItem)
        }
    }

    private fun executeSwapInternal(
        sendAmount: Double,
        receiveAmount: Double,
        sendCoin: CoinDataItem,
        receiveCoin: CoinDataItem
    ) {
        _swapLoadingData.value = LoadingData.Loading()
        swapUseCase(
            params = SwapUseCase.Params(
                sendAmount,
                receiveAmount,
                sendCoin.code,
                receiveCoin.code
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
                        val platformFee = getReceiveFee(coinToReceive, coinToReceiveDetails)
                        val platformFeeCoinsAmount = receiveAmount * platformFee
                        val atomicAmount = 1 // probably will depend on the coin type
                        val atomicSwapAmount = calcCoinsRatio(coinToSend, coinToReceive)
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
        return calcSwapAmountFromSend(
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
        return calcSwapAmountFromReceive(
            currentCoinToSend,
            currentCoinToSendDetails,
            currentCoinToReceive,
            currentCoinToReceiveDetails,
            receiveAmount
        )
    }

    private fun calcSwapAmountFromSend(
        sendCoin: CoinDataItem,
        sendCoinDetails: CoinDetailsDataItem,
        receiveCoin: CoinDataItem,
        receiveCoinDetails: CoinDetailsDataItem,
        sendAmount: Double
    ): Double {
        // Case:
        // User swap from A to B, user enter amount(A),
        // amount(B) = amount(A) x price(A) / price(B) x (1 - swapProfitPercent / 100) - fee(B)
        val receiveFee = getReceiveFee(receiveCoin, receiveCoinDetails)
        val price = sendAmount * calcCoinsRatio(sendCoin, receiveCoin)
        return price * (1 - sendCoinDetails.profitExchange / 100) - receiveFee
    }

    private fun calcSwapAmountFromReceive(
        sendCoin: CoinDataItem,
        sendCoinDetails: CoinDetailsDataItem,
        receiveCoin: CoinDataItem,
        receiveCoinDetails: CoinDetailsDataItem,
        receiveAmount: Double
    ): Double {
        // Case:
        // User swap from A to B, user enter amount(B),
        // amount(A) = (amount(B) + fee(B)) x price(B) / price(A) / (1 - swapProfitPercent / 100)
        val receiveFee = getReceiveFee(receiveCoin, receiveCoinDetails)
        val coinRatio = calcCoinsRatio(receiveCoin, sendCoin)
        return (receiveAmount + receiveFee) * coinRatio / (1 - sendCoinDetails.profitExchange / 100)
    }

    private fun getReceiveFee(
        receiveCoin: CoinDataItem,
        receiveCoinDetails: CoinDetailsDataItem,
    ): Double {
        // fee(B) = convertedTxFee(B) in case B is CATM or USDT
        // fee(B) = txFee(B) for the rest of coins.
        return when (receiveCoin.isEthRelatedCoin()) {
            true -> receiveCoinDetails.convertedTxFee
            false -> receiveCoinDetails.txFee
        }
    }

    private fun calcCoinsRatio(coin1: CoinDataItem, coin2: CoinDataItem): Double {
        return coin1.priceUsd / coin2.priceUsd
    }

    private fun initWalletObservation() {
        viewModelScope.launch {
            _initLoadingData.value = LoadingData.Loading(Unit)
            val balance = walletObserver.observe()
                .receiveAsFlow()
                .first { it != WalletBalance.NoInfo }
            when (balance) {
                is WalletBalance.Balance -> {
                    originCoinsData.clear()
                    originCoinsData.addAll(balance.data.coinList)
                    _initLoadingData.value = LoadingData.Success(Unit)
                    // move to next step
                    updateCoins(
                        originCoinsData.first { it.code == LocalCoinType.BTC.name },
                        originCoinsData.first { it.code == LocalCoinType.USDT.name }
                    )
                }
                is WalletBalance.Error -> {
                    _initLoadingData.value = LoadingData.Error(balance.error)
                }
                else -> _initLoadingData.value = LoadingData.Error(Failure.ServerError())
            }
        }
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
