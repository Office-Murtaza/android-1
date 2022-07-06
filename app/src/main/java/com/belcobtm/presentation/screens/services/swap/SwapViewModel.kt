package com.belcobtm.presentation.screens.services.swap

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belcobtm.R
import com.belcobtm.data.disk.database.account.AccountDao
import com.belcobtm.data.disk.database.account.AccountEntity
import com.belcobtm.domain.Failure
import com.belcobtm.domain.service.ServiceInfoProvider
import com.belcobtm.domain.service.ServiceType
import com.belcobtm.domain.transaction.interactor.GetFakeSignedTransactionPlanUseCase
import com.belcobtm.domain.transaction.interactor.GetMaxValueBySignedTransactionUseCase
import com.belcobtm.domain.transaction.interactor.GetSignedTransactionPlanUseCase
import com.belcobtm.domain.transaction.interactor.GetTransactionPlanUseCase
import com.belcobtm.domain.transaction.interactor.ReceiverAccountActivatedUseCase
import com.belcobtm.domain.transaction.interactor.SwapUseCase
import com.belcobtm.domain.transaction.item.AmountItem
import com.belcobtm.domain.transaction.item.SignedTransactionPlanItem
import com.belcobtm.domain.transaction.item.TransactionPlanItem
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.domain.wallet.interactor.GetCoinListUseCase
import com.belcobtm.domain.wallet.item.CoinDataItem
import com.belcobtm.domain.wallet.item.isBtcCoin
import com.belcobtm.presentation.core.SingleLiveData
import com.belcobtm.presentation.core.coin.model.ValidationResult
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.core.provider.string.StringProvider
import com.belcobtm.presentation.tools.extensions.toStringCoin
import kotlinx.coroutines.launch

class SwapViewModel(
    private val accountDao: AccountDao,
    private val getCoinListUseCase: GetCoinListUseCase,
    private val swapUseCase: SwapUseCase,
    private val serviceInfoProvider: ServiceInfoProvider,
    private val getTransactionPlanUseCase: GetTransactionPlanUseCase,
    private val getSignedTransactionPlanUseCase: GetSignedTransactionPlanUseCase,
    private val receiverAccountActivatedUseCase: ReceiverAccountActivatedUseCase,
    private val getFakeSignedTransactionPlanUseCase: GetFakeSignedTransactionPlanUseCase,
    private val getMaxValueBySignedTransactionUseCase: GetMaxValueBySignedTransactionUseCase,
    private val stringProvider: StringProvider,
) : ViewModel() {

    val originCoinsData = mutableListOf<CoinDataItem>()

    private val _coinToSend = MutableLiveData<CoinDataItem>()
    val coinToSend: LiveData<CoinDataItem> = _coinToSend

    private val _coinToReceive = MutableLiveData<CoinDataItem>()
    val coinToReceive: LiveData<CoinDataItem> = _coinToReceive

    private val _coinToSendModel = MutableLiveData<CoinPresentationModel>()
    val coinToSendModel: LiveData<CoinPresentationModel> = _coinToSendModel

    private val _coinToReceiveModel = MutableLiveData<CoinPresentationModel>()
    val coinToReceiveModel: LiveData<CoinPresentationModel> = _coinToReceiveModel

    private val _coinToReceiveError = MutableLiveData<ValidationResult>(ValidationResult.Valid)
    val coinToReceiveError: LiveData<ValidationResult> = _coinToReceiveError

    private val _usdReceiveAmount = MutableLiveData<Double>()
    val usdReceiveAmount: LiveData<Double> = _usdReceiveAmount

    private val _swapRate = MutableLiveData<SwapRateModelView>()
    val swapRate: LiveData<SwapRateModelView> = _swapRate

    private val _swapFee = MutableLiveData<SwapFeeModelView>()
    val swapFee: LiveData<SwapFeeModelView> = _swapFee

    private val _coinToSendError = MutableLiveData<ValidationResult>(ValidationResult.Valid)
    val coinToSendError: LiveData<ValidationResult> = _coinToSendError

    private val _coinsDetailsLoadingState = MutableLiveData<LoadingData<Unit>>()
    val coinsDetailsLoadingState: LiveData<LoadingData<Unit>> = _coinsDetailsLoadingState

    private val _sendCoinAmount = MutableLiveData<AmountItem>()
    val sendCoinAmount: LiveData<AmountItem> = _sendCoinAmount

    private val _sendFeeAmount = MutableLiveData<Double>()

    private val _submitEnabled = MutableLiveData(false)
    val submitEnabled: LiveData<Boolean> = _submitEnabled

    private val _receiveCoinAmount = MutableLiveData<AmountItem>()
    val receiveCoinAmount: LiveData<AmountItem> = _receiveCoinAmount

    private val _receiveFeeAmount = MutableLiveData<Double>()

    private val _swapLoadingData = SingleLiveData<LoadingData<Unit>>()
    val swapLoadingData: LiveData<LoadingData<Unit>> = _swapLoadingData

    private val _initLoadingData = MutableLiveData<LoadingData<Unit>>()
    val initLoadingData: LiveData<LoadingData<Unit>> = _initLoadingData

    private val _transactionPlanLiveData = MutableLiveData<LoadingData<Unit>>()
    val transactionPlanLiveData: LiveData<LoadingData<Unit>> = _transactionPlanLiveData

    private var fromTransactionPlanItem: TransactionPlanItem? = null
    private var signedFromTransactionPlanItem: SignedTransactionPlanItem? = null
    private var toTransactionPlanItem: TransactionPlanItem? = null
    private var signedToTransactionPlanItem: SignedTransactionPlanItem? = null

    init {
        fetchInitialData()
    }

    fun fetchInitialData() {
        viewModelScope.launch {
            _initLoadingData.value = LoadingData.Loading(Unit)
            val allCoins = accountDao.getAvailableAccounts().orEmpty()
                .filter(AccountEntity::isEnabled)
                .associateBy { it.type.name }
            getCoinListUseCase(
                params = Unit,
                onSuccess = { coinsDataList ->
                    originCoinsData.clear()
                    originCoinsData.addAll(coinsDataList.filter { allCoins[it.code] != null })
                    if (originCoinsData.size >= 2) {
                        // move to next step
                        val coinToSend = originCoinsData[0]
                        val coinToReceive = originCoinsData[1]
                        getTransactionPlanUseCase(coinToSend.code,
                            onSuccess = { fromPlanItem ->
                                fromTransactionPlanItem = fromPlanItem
                                getTransactionPlanUseCase(coinToReceive.code,
                                    onSuccess = { toPlanItem ->
                                        toTransactionPlanItem = toPlanItem
                                        updateCoinsInternal(
                                            coinToSend,
                                            coinToReceive,
                                            _initLoadingData
                                        )
                                        _initLoadingData.value = LoadingData.Success(Unit)
                                    }, onError = {
                                        _initLoadingData.value =
                                            LoadingData.Error(Failure.OperationCannotBePerformed)
                                    })
                            }, onError = {
                                _initLoadingData.value =
                                    LoadingData.Error(Failure.OperationCannotBePerformed)
                            })
                    } else {
                        _initLoadingData.value =
                            LoadingData.Error(Failure.OperationCannotBePerformed)
                    }
                },
                onError = { _initLoadingData.value = LoadingData.Error(it) }
            )
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

    fun showLocationError() {
        _swapLoadingData.value = LoadingData.Error(
            Failure.LocationError(
                stringProvider.getString(R.string.location_required_on_trade_creation)
            )
        )
    }

    fun changeCoins() {
        val coinToSend = coinToSend.value ?: return
        val coinToReceive = coinToReceive.value ?: return
        _coinToSendError.value = ValidationResult.Valid
        _coinToReceiveError.value = ValidationResult.Valid
        updateCoins(coinToReceive, coinToSend)
    }

    fun setSendAmount(sendAmount: Double) {
        if (_sendCoinAmount.value?.useMax == true) {
            val coinToSend = _coinToSend.value ?: return
            fromTransactionPlanItem?.let { plan ->
                getFakeSignedTransactionPlanUseCase(
                    GetFakeSignedTransactionPlanUseCase.Params(
                        coinToSend.code, plan, useMaxAmount = false, amount = sendAmount
                    ),
                    onSuccess = { signedTransactionPlan ->
                        signedFromTransactionPlanItem = signedTransactionPlan
                        _sendFeeAmount.value = signedTransactionPlan.fee
                    },
                    onError = { /* Failure impossible */ }
                )
            }
        }
        setSendAmountInternal(sendAmount, useMax = false)
    }

    private fun setSendAmountInternal(sendAmount: Double, useMax: Boolean) = viewModelScope.launch {
        _sendCoinAmount.value = AmountItem(sendAmount, useMax = useMax)
        _receiveCoinAmount.value =
            AmountItem(calcReceiveAmountFromSend(sendAmount), useMax = useMax)
        updatePlatformFeeInfo(sendAmount)
        validateCoinsAmount()
    }

    fun setReceiveAmount(receiveAmount: Double) {
        if (_sendCoinAmount.value?.useMax == true) {
            val coinToSend = _coinToSend.value ?: return
            toTransactionPlanItem?.let { plan ->
                getFakeSignedTransactionPlanUseCase(
                    GetFakeSignedTransactionPlanUseCase.Params(
                        coinToSend.code, plan, useMaxAmount = false, amount = receiveAmount
                    ),
                    onSuccess = { signedTransactionPlan ->
                        signedToTransactionPlanItem = signedTransactionPlan
                        _receiveFeeAmount.value = signedTransactionPlan.fee
                    },
                    onError = { /* Failure impossible */ }
                )
            }
        }
        setReceiveAmountInternal(receiveAmount, useMax = false)
    }

    private fun setReceiveAmountInternal(receiveAmount: Double, useMax: Boolean) = viewModelScope.launch {
        val sendAmount = calcSendAmountFromReceive(receiveAmount)
        _sendCoinAmount.value = AmountItem(sendAmount, useMax = useMax)
        _receiveCoinAmount.value = AmountItem(receiveAmount, useMax = useMax)
        updatePlatformFeeInfo(sendAmount)
        validateCoinsAmount()
    }

    fun setMaxSendAmount() {
        val currentCoinToSend = coinToSend.value ?: return
        val transactionPlanItem = fromTransactionPlanItem ?: return
        getMaxValueBySignedTransactionUseCase(
            GetMaxValueBySignedTransactionUseCase.Params(
                transactionPlanItem,
                currentCoinToSend,
            ),
            onSuccess = {
                _sendFeeAmount.value = it.fee
                setSendAmountInternal(it.amount, useMax = true)
            }, onError = { /* error impossible */ }
        )
    }

    fun setMaxReceiveAmount() {
        val currentCoinToSend = coinToReceive.value ?: return
        val transactionPlanItem = toTransactionPlanItem ?: return
        getMaxValueBySignedTransactionUseCase(
            GetMaxValueBySignedTransactionUseCase.Params(
                transactionPlanItem,
                currentCoinToSend,
            ),
            onSuccess = {
                _sendFeeAmount.value = it.fee
                setReceiveAmountInternal(it.amount, useMax = true)
            }, onError = { /* error impossible */ }
        )
    }

    fun executeSwap() = viewModelScope.launch {
        val sendCoinItem = coinToSend.value ?: return@launch
        val receiveCoinItem = coinToReceive.value ?: return@launch
        val sendCoinAmount = sendCoinAmount.value?.amount ?: return@launch
        val receiveCoinAmount = receiveCoinAmount.value?.amount ?: return@launch
        _coinToReceiveError.value = ValidationResult.Valid
        if (!validateCoinToSendAmount(sendCoinAmount) || !validateCoinsAmount()) {
            return@launch
        }
        val convertedValue = usdReceiveAmount.value ?: 0.0
        val service = serviceInfoProvider.getService(ServiceType.SWAP)
        if (service == null || service.txLimit < convertedValue || service.remainLimit < convertedValue) {
            _swapLoadingData.value = LoadingData.Error(
                Failure.MessageError(
                    stringProvider.getString(R.string.limits_exceeded_validation_message)
                )
            )
            return@launch
        }
        _swapLoadingData.value = LoadingData.Loading()
        if (receiveCoinItem.code == LocalCoinType.XRP.name) {
            _swapLoadingData.value = LoadingData.Loading()
            receiverAccountActivatedUseCase(
                params = ReceiverAccountActivatedUseCase.Params(
                    receiveCoinItem.details.walletAddress, receiveCoinItem.code
                ),
                onSuccess = { addressActivated ->
                    if (addressActivated) {
                        executeSwapInternal(
                            sendCoinAmount,
                            receiveCoinAmount,
                            sendCoinItem,
                            receiveCoinItem
                        )
                    } else {
                        if (receiveCoinAmount < 20 + (signedToTransactionPlanItem?.fee ?: 0.0)) {
                            _swapLoadingData.value = LoadingData.DismissProgress()
                            _coinToReceiveError.value =
                                ValidationResult.InValid(R.string.xrp_too_small_amount_error)
                        } else {
                            executeSwapInternal(
                                sendCoinAmount,
                                receiveCoinAmount,
                                sendCoinItem,
                                receiveCoinItem
                            )
                        }
                    }
                },
                onError = { _swapLoadingData.value = LoadingData.Error(it) }
            )
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
        val fromTransactionPlanItem = fromTransactionPlanItem ?: return
        val sendUseMax = _sendCoinAmount.value?.useMax ?: false
        val receiveUseMax = _receiveCoinAmount.value?.useMax ?: false
        getSignedTransactionPlanUseCase(
            GetSignedTransactionPlanUseCase.Params(
                sendCoin.details.walletAddress, sendCoin.code,
                sendAmount, fromTransactionPlanItem, sendUseMax
            ),
            onSuccess = { sendSignedPlan ->
                signedFromTransactionPlanItem = sendSignedPlan
                _sendFeeAmount.value = sendSignedPlan.fee
                getSignedTransactionPlanUseCase(
                    GetSignedTransactionPlanUseCase.Params(
                        receiveCoin.details.walletAddress, receiveCoin.code,
                        receiveAmount, fromTransactionPlanItem, receiveUseMax
                    ),
                    onSuccess = { receiveSignedPlan ->
                        viewModelScope.launch {
                            signedToTransactionPlanItem = receiveSignedPlan
                            _receiveFeeAmount.value = sendSignedPlan.fee
                            swapUseCase(
                                params = SwapUseCase.Params(
                                    useMaxAmountFlag = _sendCoinAmount.value?.useMax ?: false,
                                    coinFromAmount = sendAmount.toStringCoin().toDouble(),
                                    coinToAmount = receiveAmount.toStringCoin().toDouble(),
                                    coinFrom = sendCoin.code,
                                    fee = serviceInfoProvider.getService(ServiceType.SWAP)?.feePercent ?: 0.0,
                                    fiatAmount = sendAmount * sendCoin.priceUsd,
                                    transactionPlanItem = fromTransactionPlanItem,
                                    coinTo = receiveCoin.code
                                ),
                                onSuccess = {
                                    _swapLoadingData.value = LoadingData.Success(it)
                                },
                                onError = { _swapLoadingData.value = LoadingData.Error(it) }
                            )
                        }
                    },
                    onError = { _swapLoadingData.value = LoadingData.Error(it) }
                )
            },
            onError = { _swapLoadingData.value = LoadingData.Error(it) }
        )
    }

    private fun updateCoins(coinToSend: CoinDataItem, coinToReceive: CoinDataItem) {
        if (coinToSend == coinToReceive) {
            return
        }
        _coinToSend.value = coinToSend
        _coinToReceive.value = coinToReceive
        fetchTransactionPlans(coinToSend, coinToReceive)
    }

    private fun <T> updateCoinsInternal(
        coinToSend: CoinDataItem,
        coinToReceive: CoinDataItem,
        loadingData: MutableLiveData<LoadingData<T>>
    ) {
        if (coinToSend == coinToReceive) {
            return
        }

        val fromPlanItem = fromTransactionPlanItem ?: return
        val toPlanItem = toTransactionPlanItem ?: return

        // clear up the values to operate with 0 amount in the callbacks
        clearSendAndReceiveAmount()

        getFakeSignedTransactionPlanUseCase(
            GetFakeSignedTransactionPlanUseCase.Params(
                coinToSend.code, fromPlanItem, useMaxAmount = false, amount = 0.0
            ),
            onSuccess = { fromSignedTransactionPlan ->
                signedFromTransactionPlanItem = fromSignedTransactionPlan
                _sendFeeAmount.value = fromSignedTransactionPlan.fee
                getFakeSignedTransactionPlanUseCase(
                    GetFakeSignedTransactionPlanUseCase.Params(
                        coinToReceive.code, toPlanItem, useMaxAmount = false, amount = 0.0
                    ),
                    onSuccess = { toSignedTransactionPlan ->
                        signedFromTransactionPlanItem = toSignedTransactionPlan
                        _sendFeeAmount.value = toSignedTransactionPlan.fee
                        recalculateAmount(coinToSend, coinToReceive, fromPlanItem, toPlanItem)
                    },
                    onError = {
                        recalculateAmount(coinToSend, coinToReceive, fromPlanItem, toPlanItem)
                        loadingData.value = LoadingData.Error(it)
                    }
                )
            },
            onError = {
                recalculateAmount(coinToSend, coinToReceive, fromPlanItem, toPlanItem)
                loadingData.value = LoadingData.Error(it)
            }
        )
    }

    private fun recalculateAmount(
        coinToSend: CoinDataItem,
        coinToReceive: CoinDataItem,
        fromPlanItem: TransactionPlanItem,
        toPlanItem: TransactionPlanItem
    ) {
        _coinToSend.value = coinToSend
        _coinToReceive.value = coinToReceive
        val sendAmount = sendCoinAmount.value?.amount ?: 0.0
        val atomicSwapAmount = calcCoinsRatio(coinToSend, coinToReceive)
        _swapRate.value = SwapRateModelView(
            1,
            coinToSend.code,
            atomicSwapAmount,
            coinToReceive.code,
        )
        updatePlatformFeeInfo(sendAmount)
        _coinToSendModel.value = CoinPresentationModel(
            coinToSend.code,
            coinToSend.balanceCoin,
            fromPlanItem.txFee
        )
        _coinToReceiveModel.value = CoinPresentationModel(
            coinToReceive.code,
            coinToReceive.balanceCoin,
            toPlanItem.txFee
        )
        // notify UI that coin details has been successfully fetched
        _coinsDetailsLoadingState.value = LoadingData.Success(Unit)
    }

    private fun validateCoinToSendAmount(coinAmount: Double): Boolean {
        val validationResult = when {
            !isSufficientFromBalance() -> {
                ValidationResult.InValid(R.string.swap_screen_max_error)
            }
            coinAmount <= 0 -> {
                ValidationResult.InValid(R.string.swap_screen_min_error)
            }
            else -> {
                ValidationResult.Valid
            }
        }
        _coinToSendError.value = validationResult
        return validationResult == ValidationResult.Valid
    }

    private fun isSufficientFromBalance(): Boolean {
        val coinDataItem = coinToSend.value ?: return false
        val amount = _sendCoinAmount.value?.amount ?: 0.0
        val fee = _sendFeeAmount.value ?: 0.0
        return if (coinDataItem.isBtcCoin()) {
            amount + fee <= signedFromTransactionPlanItem?.availableAmount ?: 0.0
        } else {
            amount <= coinDataItem.balanceCoin
        }
    }

    private fun validateCoinsAmount(): Boolean {
        val sendAmount = sendCoinAmount.value?.amount ?: return false
        val receiveAmount = receiveCoinAmount.value?.amount ?: return false
        val amountsArePositive = sendAmount > 0 && receiveAmount > 0
        return amountsArePositive.also { _submitEnabled.value = it }
    }

    private suspend fun calcReceiveAmountFromSend(sendAmount: Double): Double {
        val currentCoinToSend = coinToSend.value ?: return 0.0
        val currentCoinToReceive = coinToReceive.value ?: return 0.0
        return calcSwapAmountFromSend(
            currentCoinToSend,
            currentCoinToReceive,
            sendAmount
        )
    }

    private suspend fun calcSendAmountFromReceive(receiveAmount: Double): Double {
        val currentCoinToSend = coinToSend.value ?: return 0.0
        val currentCoinToReceive = coinToReceive.value ?: return 0.0
        return calcSwapAmountFromReceive(
            currentCoinToSend,
            currentCoinToReceive,
            receiveAmount
        )
    }

    private suspend fun calcSwapAmountFromSend(
        sendCoin: CoinDataItem,
        receiveCoin: CoinDataItem,
        sendAmount: Double
    ): Double {
        // Case:
        // User swap from A to B, user enter amount(A),
        // amount(B) = amount(A) x price(A) / price(B) x (1 - swapProfitPercent / 100) - fee(B)
        val price = sendAmount * calcCoinsRatio(sendCoin, receiveCoin)
        val receiveCoinFee = getReceiveCoinFee()
        val sendCoinFee = getCoinFeeActual()
        val result = price * sendCoinFee - receiveCoinFee
        return 0.0.coerceAtLeast(result)
    }

    private suspend fun calcSwapAmountFromReceive(
        sendCoin: CoinDataItem,
        receiveCoin: CoinDataItem,
        receiveAmount: Double
    ): Double {
        // Case:
        // User swap from A to B, user enter amount(B),
        // amount(A) = (amount(B) + fee(B)) x price(B) / price(A) / (1 - swapProfitPercent / 100)
        val receiveCoinFee = getReceiveCoinFee()
        val coinRatio = calcCoinsRatio(receiveCoin, sendCoin)
        val sendCoinFee = getCoinFeeActual()
        val result = (receiveAmount + receiveCoinFee) * coinRatio / sendCoinFee
        return 0.0.coerceAtLeast(result)
    }

    private fun getReceiveCoinFee(): Double {
        // fee(B) = convertedTxFee(B) in case B is CATM or USDC
        // fee(B) = txFee(B) for the rest of coins.
        val toPlanItem = signedToTransactionPlanItem ?: return 0.0
        return toPlanItem.fee
    }

    private suspend fun getCoinFeeActual(): Double {
        return 1 - (serviceInfoProvider.getService(ServiceType.SWAP)?.feePercent ?: 0.0) / 100
    }

    private fun calcCoinsRatio(coin1: CoinDataItem, coin2: CoinDataItem): Double {
        return coin1.priceUsd / coin2.priceUsd
    }

    private fun updatePlatformFeeInfo(sendAmount: Double) = viewModelScope.launch {
        val coinToSend = coinToSend.value ?: return@launch
        val coinToReceive = coinToReceive.value ?: return@launch
        // Platform fee(B) = amount(A) x price(A) / price(B) x (swapProfitPercent / 100)
        val receiveRawAmount = sendAmount * calcCoinsRatio(coinToSend, coinToReceive)
        val platformFeeActual = serviceInfoProvider.getService(ServiceType.SWAP)?.feePercent ?: 0.0
        val platformFeeCoinsAmount = receiveRawAmount * (platformFeeActual / 100)
        _swapFee.value = SwapFeeModelView(
            platformFeeActual,
            platformFeeCoinsAmount,
            coinToReceive.code
        )
        _usdReceiveAmount.value = calcReceiveAmountFromSend(sendAmount) * coinToReceive.priceUsd
    }

    private fun clearSendAndReceiveAmount() {
        _sendCoinAmount.value = AmountItem(0.0, useMax = false)
        _receiveCoinAmount.value = AmountItem(0.0, useMax = false)
    }

    fun reFetchTransactionPlans() {
        val coinToSend = coinToSend.value ?: return
        val coinToReceive = coinToReceive.value ?: return
        fetchTransactionPlans(coinToSend, coinToReceive)
    }

    private fun fetchTransactionPlans(
        coinToSend: CoinDataItem,
        coinToReceive: CoinDataItem
    ) {
        _transactionPlanLiveData.value = LoadingData.Loading()
        if (coinToSend.code == fromTransactionPlanItem?.coinCode) {
            getTransactionPlanUseCase(
                coinToReceive.code,
                onSuccess = { toPlan ->
                    toTransactionPlanItem = toPlan
                    updateCoinsInternal(coinToSend, coinToReceive, _transactionPlanLiveData)
                    _transactionPlanLiveData.value = LoadingData.Success(Unit)
                },
                onError = {
                    _transactionPlanLiveData.value = LoadingData.Error(it)
                }
            )
        } else {
            getTransactionPlanUseCase(
                coinToSend.code,
                onSuccess = { fromPlan ->
                    fromTransactionPlanItem = fromPlan
                    updateCoinsInternal(coinToSend, coinToReceive, _transactionPlanLiveData)
                    _transactionPlanLiveData.value = LoadingData.Success(Unit)
                },
                onError = {
                    _transactionPlanLiveData.value = LoadingData.Error(it)
                }
            )
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

data class CoinPresentationModel(
    val coinCode: String,
    val coinBalance: Double,
    val coinFee: Double
)
