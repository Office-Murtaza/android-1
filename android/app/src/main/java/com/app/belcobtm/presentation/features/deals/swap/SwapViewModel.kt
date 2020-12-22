package com.app.belcobtm.presentation.features.deals.swap

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.belcobtm.R
import com.app.belcobtm.data.disk.database.AccountDao
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.transaction.interactor.CheckXRPAddressActivatedUseCase
import com.app.belcobtm.domain.transaction.interactor.SwapUseCase
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.domain.wallet.interactor.GetCoinDetailsUseCase
import com.app.belcobtm.domain.wallet.interactor.GetFreshCoinsUseCase
import com.app.belcobtm.domain.wallet.item.CoinDataItem
import com.app.belcobtm.domain.wallet.item.CoinDetailsDataItem
import com.app.belcobtm.domain.wallet.item.isEthRelatedCoin
import com.app.belcobtm.presentation.core.coin.AmountCoinValidator
import com.app.belcobtm.presentation.core.coin.MinMaxCoinValueProvider
import com.app.belcobtm.presentation.core.coin.model.ValidationResult
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import kotlinx.coroutines.launch

class SwapViewModel(
    private val accountDao: AccountDao,
    private val getFreshCoinsUseCase: GetFreshCoinsUseCase,
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

    private val _coinToSendError = MutableLiveData<ValidationResult>(ValidationResult.Valid)
    val coinToSendError: LiveData<ValidationResult> = _coinToSendError

    private val _coinsDetailsLoadingState = MutableLiveData<LoadingData<Unit>>()
    val coinsDetailsLoadingState: LiveData<LoadingData<Unit>> = _coinsDetailsLoadingState

    private val _sendCoinAmount = MutableLiveData<Double>(0.0)
    val sendCoinAmount: LiveData<Double> = _sendCoinAmount

    private val _submitEnabled = MutableLiveData(false)
    val submitEnabled: LiveData<Boolean> = _submitEnabled

    private val _receiveCoinAmount = MutableLiveData<Double>(0.0)
    val receiveCoinAmount: LiveData<Double> = _receiveCoinAmount

    private val _swapLoadingData = MutableLiveData<LoadingData<Unit>>()
    val swapLoadingData: LiveData<LoadingData<Unit>> = _swapLoadingData

    private val _initLoadingData = MutableLiveData<LoadingData<Unit>>()
    val initLoadingData: LiveData<LoadingData<Unit>> = _initLoadingData

    init {
        fetchInitialData()
    }

    fun fetchInitialData() {
        viewModelScope.launch {
            _initLoadingData.value = LoadingData.Loading(Unit)
            val allCoins = accountDao.getItemList().orEmpty()
            if (allCoins.isNotEmpty()) {
                val coinCodesList = allCoins.map { it.type.name }
                getFreshCoinsUseCase(
                    params = GetFreshCoinsUseCase.Params(coinCodesList),
                    onSuccess = { coinsDataList ->
                        originCoinsData.clear()
                        originCoinsData.addAll(coinsDataList)
                        _initLoadingData.value = LoadingData.Success(Unit)
                        // move to next step
                        updateCoins(
                            originCoinsData.first { it.code == LocalCoinType.BTC.name },
                            originCoinsData.first { it.code == LocalCoinType.USDT.name }
                        )
                    },
                    onError = { _initLoadingData.value = LoadingData.Error(Failure.ServerError()) }
                )
            } else {
                _initLoadingData.value = LoadingData.Error(Failure.ServerError())
            }
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

    fun changeCoins() {
        val coinToSend = coinToSend.value ?: return
        val coinToReceive = coinToReceive.value ?: return
        updateCoins(coinToReceive, coinToSend)
    }

    fun setSendAmount(sendAmount: Double) {
        _sendCoinAmount.value = sendAmount
        _receiveCoinAmount.value = calcReceiveAmountFromSend(sendAmount)
        updateFeeInfo(sendAmount)
        validateCoinsAmount()
    }

    fun setReceiveAmount(receiveAmount: Double) {
        val sendAmount = calcSendAmountFromReceive(receiveAmount)
        _receiveCoinAmount.value = receiveAmount
        _sendCoinAmount.value = sendAmount
        updateFeeInfo(sendAmount)
        validateCoinsAmount()
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
        if (!validateCoinToSendAmount(sendCoinAmount) || !validateCoinsAmount()) {
            return
        }
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
        // clear up the values to operate with 0 amount in the callbacks
        clearSendAndReceiveAmount()
        // start fetching only after clearing up amount values
        getCoinDetailsUseCase(
            params = GetCoinDetailsUseCase.Params(coinToSend.code),
            onSuccess = { coinToSendDetails ->
                getCoinDetailsUseCase(
                    params = GetCoinDetailsUseCase.Params(coinToReceive.code),
                    onSuccess = { coinToReceiveDetails ->
                        _coinToSend.value = coinToSend
                        _coinToReceive.value = coinToReceive
                        this.coinToSendDetails = coinToSendDetails
                        this.coinToReceiveDetails = coinToReceiveDetails
                        val sendAmount = sendCoinAmount.value ?: 0.0
                        val atomicSwapAmount = calcCoinsRatio(coinToSend, coinToReceive)
                        _swapRate.value = SwapRateModelView(
                            1,
                            coinToSend.code,
                            atomicSwapAmount,
                            coinToReceive.code
                        )
                        updateFeeInfo(sendAmount)
                        // notify UI that coin details has beed successfully fetched
                        _coinsDetailsLoadingState.value = LoadingData.Success(Unit)
                    },
                    onError = { _coinsDetailsLoadingState.value = LoadingData.Error(it) }
                )
            },
            onError = { _coinsDetailsLoadingState.value = LoadingData.Error(it) }
        )
    }

    private fun validateCoinToSendAmount(coinAmount: Double): Boolean {
        val currentCoinToSend = coinToSend.value ?: return false
        val currentCoinToSendDetails = coinToSendDetails ?: return false
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
        return validationResult == ValidationResult.Valid
    }

    private fun validateCoinsAmount(): Boolean {
        val sendAmount = sendCoinAmount.value ?: return false
        val receiveAmount = receiveCoinAmount.value ?: return false
        val amountsArePositive =  sendAmount > 0 && receiveAmount > 0
        return amountsArePositive.also { _submitEnabled.value = it }
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
        val price = sendAmount * calcCoinsRatio(sendCoin, receiveCoin)
        val receiveCoinFee = getReceiveCoinFee(receiveCoin, receiveCoinDetails)
        val sendCoinFee = getSendCoinFee(sendCoinDetails)
        return price * sendCoinFee - receiveCoinFee
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
        val receiveCoinFee = getReceiveCoinFee(receiveCoin, receiveCoinDetails)
        val coinRatio = calcCoinsRatio(receiveCoin, sendCoin)
        val sendCoinFee = getSendCoinFee(sendCoinDetails)
        return (receiveAmount + receiveCoinFee) * coinRatio / sendCoinFee
    }

    private fun getReceiveCoinFee(
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

    private fun getSendCoinFee(coinDetailsDataItem: CoinDetailsDataItem): Double {
        return 1 - coinDetailsDataItem.profitExchange / 100
    }

    private fun calcCoinsRatio(coin1: CoinDataItem, coin2: CoinDataItem): Double {
        return coin1.priceUsd / coin2.priceUsd
    }

    private fun updateFeeInfo(sendAmount: Double) {
        val coinToSend = coinToSend.value ?: return
        val coinToReceive = coinToReceive.value ?: return
        val receiveRawAmount = sendAmount * calcCoinsRatio(coinToSend, coinToReceive)
        val receiveWithFeeAmount = calcReceiveAmountFromSend(sendAmount)
        val platformFeeCoinsAmount = receiveRawAmount - receiveWithFeeAmount
        val platformFeeActual = platformFeeCoinsAmount / receiveRawAmount
        if (platformFeeActual.isFinite()) {
            _swapFee.value = SwapFeeModelView(
                platformFeeActual,
                platformFeeCoinsAmount,
                coinToReceive.code
            )
        } else {
            _swapFee.value = SwapFeeModelView(
                0.0,
                0.0,
                coinToReceive.code
            )
        }
    }

    private fun clearSendAndReceiveAmount() {
        _sendCoinAmount.value = 0.0
        _receiveCoinAmount.value = 0.0
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
