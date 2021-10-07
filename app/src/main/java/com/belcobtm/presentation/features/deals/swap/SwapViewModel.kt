package com.belcobtm.presentation.features.deals.swap

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belcobtm.R
import com.belcobtm.data.disk.database.account.AccountDao
import com.belcobtm.data.disk.database.account.AccountEntity
import com.belcobtm.data.disk.database.service.ServiceType
import com.belcobtm.domain.Failure
import com.belcobtm.domain.service.ServiceInfoProvider
import com.belcobtm.domain.transaction.interactor.CheckXRPAddressActivatedUseCase
import com.belcobtm.domain.transaction.interactor.GetTransactionPlanUseCase
import com.belcobtm.domain.transaction.interactor.SwapUseCase
import com.belcobtm.domain.transaction.item.TransactionPlanItem
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.domain.wallet.interactor.GetCoinListUseCase
import com.belcobtm.domain.wallet.item.CoinDataItem
import com.belcobtm.domain.wallet.item.isEthRelatedCoin
import com.belcobtm.presentation.core.SingleLiveData
import com.belcobtm.presentation.core.coin.AmountCoinValidator
import com.belcobtm.presentation.core.coin.CoinLimitsValueProvider
import com.belcobtm.presentation.core.coin.model.ValidationResult
import com.belcobtm.presentation.core.mvvm.LoadingData
import kotlinx.coroutines.launch

class SwapViewModel(
    private val accountDao: AccountDao,
    private val getCoinListUseCase: GetCoinListUseCase,
    private val amountValidator: AmountCoinValidator,
    private val swapUseCase: SwapUseCase,
    private val checkXRPAddressActivatedUseCase: CheckXRPAddressActivatedUseCase,
    private val coinLimitsValueProvider: CoinLimitsValueProvider,
    private val serviceInfoProvider: ServiceInfoProvider,
    private val getTransactionPlanUseCase: GetTransactionPlanUseCase
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

    private val _sendCoinAmount = MutableLiveData(0.0)
    val sendCoinAmount: LiveData<Double> = _sendCoinAmount

    private val _submitEnabled = MutableLiveData(false)
    val submitEnabled: LiveData<Boolean> = _submitEnabled

    private val _receiveCoinAmount = MutableLiveData(0.0)
    val receiveCoinAmount: LiveData<Double> = _receiveCoinAmount

    private val _swapLoadingData = SingleLiveData<LoadingData<Unit>>()
    val swapLoadingData: LiveData<LoadingData<Unit>> = _swapLoadingData

    private val _initLoadingData = MutableLiveData<LoadingData<Unit>>()
    val initLoadingData: LiveData<LoadingData<Unit>> = _initLoadingData

    private val _transactionPlanLiveData = MutableLiveData<LoadingData<Unit>>()
    val transactionPlanLiveData: LiveData<LoadingData<Unit>> = _transactionPlanLiveData

    private var fromTransactionPlanItem: TransactionPlanItem? = null
    private var toTransactionPlanItem: TransactionPlanItem? = null

    init {
        fetchInitialData()
    }

    fun fetchInitialData() {
        viewModelScope.launch {
            _initLoadingData.value = LoadingData.Loading(Unit)
            val allCoins = accountDao.getItemList().orEmpty()
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
                                        _initLoadingData.value = LoadingData.Success(Unit)
                                        updateCoinsInternal(coinToSend, coinToReceive)
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

    fun changeCoins() {
        val coinToSend = coinToSend.value ?: return
        val coinToReceive = coinToReceive.value ?: return
        _coinToSendError.value = ValidationResult.Valid
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
        val transactionPlanItem = fromTransactionPlanItem ?: return
        val maxAmount = coinLimitsValueProvider
            .getMaxValue(currentCoinToSend, transactionPlanItem.txFee)
        setSendAmount(maxAmount)
    }

    fun executeSwap() {
        val sendCoinItem = coinToSend.value ?: return
        val receiveCoinItem = coinToReceive.value ?: return
        val sendCoinAmount = sendCoinAmount.value ?: return
        val receiveCoinAmount = receiveCoinAmount.value ?: return
        if (!validateCoinToSendAmount(sendCoinAmount) || !validateCoinsAmount()) {
            return
        }
        if (receiveCoinItem.code == LocalCoinType.XRP.name) {
            val minXRPValue = 20
            if (receiveCoinAmount < minXRPValue) {
                _swapLoadingData.value = LoadingData.Loading()
                checkXRPAddressActivatedUseCase(
                    params = CheckXRPAddressActivatedUseCase.Param(receiveCoinItem.details.walletAddress),
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
        val transactionPlanItem = fromTransactionPlanItem ?: return
        _swapLoadingData.value = LoadingData.Loading()
        swapUseCase(
            params = SwapUseCase.Params(
                sendAmount,
                receiveAmount,
                sendCoin.code,
                _swapFee.value?.platformFeeCoinAmount ?: 0.0,
                transactionPlanItem,
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
        _coinToSend.value = coinToSend
        _coinToReceive.value = coinToReceive
        fetchTransactionPlans(coinToSend, coinToReceive)
    }

    private fun updateCoinsInternal(coinToSend: CoinDataItem, coinToReceive: CoinDataItem) {
        if (coinToSend == coinToReceive) {
            return
        }

        val fromPlanItem = fromTransactionPlanItem ?: return
        val toPlanItem = toTransactionPlanItem ?: return

        // clear up the values to operate with 0 amount in the callbacks
        clearSendAndReceiveAmount()
        _coinToSend.value = coinToSend
        _coinToReceive.value = coinToReceive
        val sendAmount = sendCoinAmount.value ?: 0.0
        val atomicSwapAmount = calcCoinsRatio(coinToSend, coinToReceive)
        _swapRate.value = SwapRateModelView(
            1,
            coinToSend.code,
            atomicSwapAmount,
            coinToReceive.code,
        )
        updateFeeInfo(sendAmount)
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
        // notify UI that coin details has beed successfully fetched
        _coinsDetailsLoadingState.value = LoadingData.Success(Unit)
    }

    private fun validateCoinToSendAmount(coinAmount: Double): Boolean {
        val currentCoinToSend = coinToSend.value ?: return false
        val transactionPlanItem = fromTransactionPlanItem ?: return false
        val balanceValidationResult = amountValidator.validateBalance(
            coinAmount, currentCoinToSend, originCoinsData
        )
        val maxCoinAmount = coinLimitsValueProvider
            .getMaxValue(currentCoinToSend, transactionPlanItem.txFee)
        val validationResult = when {
            balanceValidationResult is ValidationResult.InValid -> {
                balanceValidationResult
            }
            coinAmount > maxCoinAmount -> {
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

    private fun validateCoinsAmount(): Boolean {
        val sendAmount = sendCoinAmount.value ?: return false
        val receiveAmount = receiveCoinAmount.value ?: return false
        val amountsArePositive = sendAmount > 0 && receiveAmount > 0
        return amountsArePositive.also { _submitEnabled.value = it }
    }

    private fun calcReceiveAmountFromSend(sendAmount: Double): Double {
        val currentCoinToSend = coinToSend.value ?: return 0.0
        val currentCoinToReceive = coinToReceive.value ?: return 0.0
        return calcSwapAmountFromSend(
            currentCoinToSend,
            currentCoinToReceive,
            sendAmount
        )
    }

    private fun calcSendAmountFromReceive(receiveAmount: Double): Double {
        val currentCoinToSend = coinToSend.value ?: return 0.0
        val currentCoinToReceive = coinToReceive.value ?: return 0.0
        return calcSwapAmountFromReceive(
            currentCoinToSend,
            currentCoinToReceive,
            receiveAmount
        )
    }

    private fun calcSwapAmountFromSend(
        sendCoin: CoinDataItem,
        receiveCoin: CoinDataItem,
        sendAmount: Double
    ): Double {
        // Case:
        // User swap from A to B, user enter amount(A),
        // amount(B) = amount(A) x price(A) / price(B) x (1 - swapProfitPercent / 100) - fee(B)
        val price = sendAmount * calcCoinsRatio(sendCoin, receiveCoin)
        val receiveCoinFee = getReceiveCoinFee(receiveCoin)
        val sendCoinFee = getCoinFeeActual()
        val result = price * sendCoinFee - receiveCoinFee
        return 0.0.coerceAtLeast(result)
    }

    private fun calcSwapAmountFromReceive(
        sendCoin: CoinDataItem,
        receiveCoin: CoinDataItem,
        receiveAmount: Double
    ): Double {
        // Case:
        // User swap from A to B, user enter amount(B),
        // amount(A) = (amount(B) + fee(B)) x price(B) / price(A) / (1 - swapProfitPercent / 100)
        val receiveCoinFee = getReceiveCoinFee(receiveCoin)
        val coinRatio = calcCoinsRatio(receiveCoin, sendCoin)
        val sendCoinFee = getCoinFeeActual()
        val result = (receiveAmount + receiveCoinFee) * coinRatio / sendCoinFee
        return 0.0.coerceAtLeast(result)
    }

    private fun getReceiveCoinFee(
        receiveCoin: CoinDataItem
    ): Double {
        // fee(B) = convertedTxFee(B) in case B is CATM or USDC
        // fee(B) = txFee(B) for the rest of coins.
        val toPlanItem = toTransactionPlanItem ?: return 0.0
        return when (receiveCoin.isEthRelatedCoin()) {
            true -> toPlanItem.nativeTxFee
            false -> toPlanItem.txFee
        }
    }

    private fun getCoinFeeActual(): Double {
        return 1 - serviceInfoProvider.getServiceFee(ServiceType.SWAP) / 100
    }

    private fun calcCoinsRatio(coin1: CoinDataItem, coin2: CoinDataItem): Double {
        return coin1.priceUsd / coin2.priceUsd
    }

    private fun updateFeeInfo(sendAmount: Double) {
        val coinToSend = coinToSend.value ?: return
        val coinToReceive = coinToReceive.value ?: return
        // Platform fee(B) = amount(A) x price(A) / price(B) x (swapProfitPercent / 100)
        val receiveRawAmount = sendAmount * calcCoinsRatio(coinToSend, coinToReceive)
        val platformFeeActual = serviceInfoProvider.getServiceFee(ServiceType.SWAP)
        val platformFeeCoinsAmount = receiveRawAmount * (platformFeeActual / 100)
        _swapFee.value = SwapFeeModelView(
            platformFeeActual,
            platformFeeCoinsAmount,
            coinToReceive.code
        )
        _usdReceiveAmount.value = calcReceiveAmountFromSend(sendAmount) * coinToReceive.priceUsd
    }

    private fun clearSendAndReceiveAmount() {
        _sendCoinAmount.value = 0.0
        _receiveCoinAmount.value = 0.0
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
                    updateCoinsInternal(coinToSend, coinToReceive)
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
                    updateCoinsInternal(coinToSend, coinToReceive)
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
