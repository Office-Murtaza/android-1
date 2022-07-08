package com.belcobtm.presentation.screens.wallet.recall

import android.text.Editable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.belcobtm.domain.transaction.interactor.GetTransactionPlanUseCase
import com.belcobtm.domain.transaction.interactor.trade.TradeRecallTransactionCompleteUseCase
import com.belcobtm.domain.transaction.item.TransactionPlanItem
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.domain.wallet.interactor.GetCoinByCodeUseCase
import com.belcobtm.domain.wallet.item.CoinDataItem
import com.belcobtm.domain.wallet.item.isEthRelatedCoin
import com.belcobtm.presentation.core.coin.CoinCodeProvider
import com.belcobtm.presentation.core.item.CoinScreenItem
import com.belcobtm.presentation.core.item.mapToScreenItem
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.screens.wallet.reserve.InputFieldState
import com.belcobtm.presentation.tools.extensions.withScale

class TradeRecallViewModel(
    private val coinCode: String,
    private val getCoinByCodeUseCase: GetCoinByCodeUseCase,
    private val completeTransactionUseCase: TradeRecallTransactionCompleteUseCase,
    private val coinCodeProvider: CoinCodeProvider,
    private val getTransactionPlanUseCase: GetTransactionPlanUseCase
) : ViewModel() {

    private var transactionPlanItem: TransactionPlanItem? = null

    private val _initialLoadLiveData = MutableLiveData<LoadingData<Unit>>()
    val initialLoadLiveData: LiveData<LoadingData<Unit>> = _initialLoadLiveData

    private val _transactionLiveData = MutableLiveData<LoadingData<Unit>>()
    val transactionLiveData: LiveData<LoadingData<Unit>> = _transactionLiveData

    private val _cryptoFieldState = MutableLiveData<InputFieldState>()
    val cryptoFieldState: LiveData<InputFieldState> = _cryptoFieldState

    private val _fee = MutableLiveData<Double>()
    val fee: LiveData<Double>
        get() = _fee

    private val _isSubmitButtonEnabled = MutableLiveData<Boolean>()
    val isSubmitButtonEnabled: LiveData<Boolean> = _isSubmitButtonEnabled

    private var etheriumCoinDataItem: CoinDataItem? = null
    private lateinit var coinDataItem: CoinDataItem
    lateinit var coinItem: CoinScreenItem
        private set

    var selectedAmount: Double = 0.0

    init {
        loadInitialData()
    }

    fun loadInitialData() {
        _initialLoadLiveData.value = LoadingData.Loading()
        getCoinByCodeUseCase.invoke(coinCode, onSuccess = { coinItem ->
            this.coinDataItem = coinItem
            this.coinItem = coinItem.mapToScreenItem()
            getTransactionPlanUseCase(coinCode, onSuccess = { transactionPlan ->
                transactionPlanItem = transactionPlan
                _fee.value = transactionPlan.txFee
                if (coinItem.isEthRelatedCoin()) {
                    // for CATM amount calculation we need ETH coin
                    fetchEtherium()
                }
            }, onError = {
                _initialLoadLiveData.value = LoadingData.Error(it)
            })
            _initialLoadLiveData.value = LoadingData.Success(Unit)
        }, onError = {
            _initialLoadLiveData.value = LoadingData.Error(it)
        })
    }

    fun performTransaction() {
        if (!validateCryptoAmount()) {
            return
        }
        _transactionLiveData.value = LoadingData.Loading()
        completeTransactionUseCase.invoke(
            params = TradeRecallTransactionCompleteUseCase.Params(
                coinCode = coinDataItem.code,
                cryptoAmount = selectedAmount,
                price = coinDataItem.priceUsd,
                fiatAmount = selectedAmount * coinDataItem.priceUsd
            ),
            onSuccess = {
                // we need to add some delay as server returns 200 before writting to DB
                _transactionLiveData.value = LoadingData.Success(Unit)
            },
            onError = { _transactionLiveData.value = LoadingData.Error(it) }
        )
    }

    private fun validateCryptoAmount(): Boolean {
        val maxValue = getMaxValue()
        val enoughETHForExtraFee = enoughETHForExtraFee(selectedAmount)
        when {
            selectedAmount > maxValue ->
                _cryptoFieldState.value = InputFieldState.MoreThanNeedError
            selectedAmount <= 0 ->
                _cryptoFieldState.value = InputFieldState.LessThanNeedError
            enoughETHForExtraFee.not() ->
                _cryptoFieldState.value = InputFieldState.NotEnoughETHError
            selectedAmount in 0.0..maxValue && enoughETHForExtraFee -> {
                _cryptoFieldState.value = InputFieldState.Valid
                return true
            }
        }
        return false
    }

    fun getCoinCode(): String = coinCodeProvider.getCoinCode(coinDataItem)

    fun getMaxValue(): Double =
        if (coinDataItem.isEthRelatedCoin()) {
            coinDataItem.reservedBalanceCoin - (transactionPlanItem?.nativeTxFee ?: 0.0)
        } else {
            coinDataItem.reservedBalanceCoin - (transactionPlanItem?.txFee ?: 0.0)
        }

    private fun enoughETHForExtraFee(currentCryptoAmount: Double): Boolean {
        if (coinDataItem.isEthRelatedCoin()) {
            val controlValue =
                0.0 * etheriumCoinDataItem!!.priceUsd / coinDataItem.priceUsd
            return currentCryptoAmount <= controlValue.withScale(0)
        }
        return true
    }

    private fun fetchEtherium() {
        getCoinByCodeUseCase(
            params = LocalCoinType.ETH.name,
            onSuccess = {
                etheriumCoinDataItem = it
            },
            onError = { _initialLoadLiveData.value = LoadingData.Error(it) }
        )
    }

    fun checkAmountInput(editable: Editable?) {
        _isSubmitButtonEnabled.value = editable.isNullOrEmpty().not()
    }

}
