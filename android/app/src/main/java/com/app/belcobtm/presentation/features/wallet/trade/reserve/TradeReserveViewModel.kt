package com.app.belcobtm.presentation.features.wallet.trade.reserve

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.belcobtm.domain.transaction.interactor.trade.TradeReserveTransactionCompleteUseCase
import com.app.belcobtm.domain.transaction.interactor.trade.TradeReserveTransactionCreateUseCase
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.domain.wallet.interactor.GetCoinByCodeUseCase
import com.app.belcobtm.domain.wallet.item.CoinDataItem
import com.app.belcobtm.domain.wallet.item.isEthRelatedCoin
import com.app.belcobtm.presentation.core.coin.AmountCoinValidator
import com.app.belcobtm.presentation.core.coin.CoinCodeProvider
import com.app.belcobtm.presentation.core.coin.CoinLimitsValueProvider
import com.app.belcobtm.presentation.core.coin.model.ValidationResult
import com.app.belcobtm.presentation.core.item.CoinScreenItem
import com.app.belcobtm.presentation.core.item.mapToScreenItem
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TradeReserveViewModel(
    private val coinCode: String,
    private val getCoinByCodeUseCase: GetCoinByCodeUseCase,
    private val createTransactionUseCase: TradeReserveTransactionCreateUseCase,
    private val completeTransactionUseCase: TradeReserveTransactionCompleteUseCase,
    private val coinLimitsValueProvider: CoinLimitsValueProvider,
    private val amountCoinValidator: AmountCoinValidator,
    private val coinCodeProvider: CoinCodeProvider
) : ViewModel() {
    private val _initialLoadLiveData = MutableLiveData<LoadingData<Unit>>()
    val initialLoadLiveData: LiveData<LoadingData<Unit>> = _initialLoadLiveData

    private val _createTransactionLiveData = MutableLiveData<LoadingData<Unit>>()
    val createTransactionLiveData: LiveData<LoadingData<Unit>> = _createTransactionLiveData

    private val _cryptoFieldState = MutableLiveData<InputFieldState>()
    val cryptoFieldState: LiveData<InputFieldState> = _cryptoFieldState

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
            if (coinItem.isEthRelatedCoin()) {
                // for CATM amount calculation we need ETH coin
                fetchEtherium()
            } else {
                _initialLoadLiveData.value = LoadingData.Success(Unit)
            }
        }, onError = {
            _initialLoadLiveData.value = LoadingData.Error(it)
        })
    }

    fun createTransaction() {
        if (!validateCryptoAmount()) {
            return
        }
        _createTransactionLiveData.value = LoadingData.Loading()
        createTransactionUseCase.invoke(
            params = TradeReserveTransactionCreateUseCase.Params(coinDataItem.code, selectedAmount),
            onSuccess = { completeTransaction(it) },
            onError = { _createTransactionLiveData.value = LoadingData.Error(it) }
        )
    }

    private fun completeTransaction(hash: String) {
        _createTransactionLiveData.value = LoadingData.Loading()
        completeTransactionUseCase.invoke(
            params = TradeReserveTransactionCompleteUseCase.Params(
                coinDataItem.code,
                selectedAmount,
                hash
            ),
            onSuccess = {
                // we need to add some delay as server returns 200 before writting to DB
                viewModelScope.launch {
                    delay(1000)
                    _createTransactionLiveData.value = LoadingData.Success(Unit)
                }
            },
            onError = { _createTransactionLiveData.value = LoadingData.Error(it) }
        )
    }

    fun getTransactionFee(): Double = coinDataItem.details.txFee

    fun getCoinCode(): String = coinCodeProvider.getCoinCode(coinDataItem)

    fun getMaxValue(): Double =
        coinLimitsValueProvider.getMaxValue(coinDataItem)

    private fun validateCryptoAmount(): Boolean {
        val maxValue = getMaxValue()
        val enoughETHForExtraFee = enoughETHForExtraFee()
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

    private fun enoughETHForExtraFee(): Boolean {
        val coinList = etheriumCoinDataItem?.let(::listOf).orEmpty()
        val validationResult = amountCoinValidator.validateBalance(
            amount = 0.0,
            coin = coinDataItem,
            coinList = coinList
        )
        return validationResult is ValidationResult.Valid
    }

    private fun fetchEtherium() {
        getCoinByCodeUseCase.invoke(
            params = LocalCoinType.ETH.name,
            onSuccess = {
                etheriumCoinDataItem = it
                _initialLoadLiveData.value = LoadingData.Success(Unit)
            },
            onError = { _initialLoadLiveData.value = LoadingData.Error(it) }
        )
    }
}
