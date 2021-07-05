package com.belcobtm.presentation.features.wallet.trade.recall

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belcobtm.domain.transaction.interactor.trade.TradeRecallTransactionCompleteUseCase
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.domain.wallet.interactor.GetCoinByCodeUseCase
import com.belcobtm.domain.wallet.item.CoinDataItem
import com.belcobtm.domain.wallet.item.isEthRelatedCoin
import com.belcobtm.presentation.core.coin.CoinCodeProvider
import com.belcobtm.presentation.core.coin.CoinLimitsValueProvider
import com.belcobtm.presentation.core.extensions.withScale
import com.belcobtm.presentation.core.item.CoinScreenItem
import com.belcobtm.presentation.core.item.mapToScreenItem
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.features.wallet.trade.reserve.InputFieldState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TradeRecallViewModel(
    private val coinCode: String,
    private val getCoinByCodeUseCase: GetCoinByCodeUseCase,
    private val completeTransactionUseCase: TradeRecallTransactionCompleteUseCase,
    private val coinCodeProvider: CoinCodeProvider,
    private val coinLimitsValueProvider: CoinLimitsValueProvider
) : ViewModel() {
    private val _initialLoadLiveData = MutableLiveData<LoadingData<Unit>>()
    val initialLoadLiveData: LiveData<LoadingData<Unit>> = _initialLoadLiveData

    private val _transactionLiveData = MutableLiveData<LoadingData<Unit>>()
    val transactionLiveData: LiveData<LoadingData<Unit>> = _transactionLiveData

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

    fun performTransaction() {
        if (!validateCryptoAmount()) {
            return
        }
        _transactionLiveData.value = LoadingData.Loading()
        completeTransactionUseCase.invoke(
            params = TradeRecallTransactionCompleteUseCase.Params(
                coinDataItem.code,
                selectedAmount
            ),
            onSuccess = {
                // we need to add some delay as server returns 200 before writting to DB
                viewModelScope.launch {
                    delay(1000)
                    _transactionLiveData.value = LoadingData.Success(Unit)
                }
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

    fun getTransactionFee(): Double = coinDataItem.details.txFee

    fun getCoinCode(): String = coinCodeProvider.getCoinCode(coinDataItem)

    fun getMaxValue(): Double = coinLimitsValueProvider.getMaxValue(coinDataItem, useReservedBalance = true)

    private fun enoughETHForExtraFee(currentCryptoAmount: Double): Boolean {
        if (coinDataItem.isEthRelatedCoin()) {
            val controlValue =
                coinDataItem.details.txFee * etheriumCoinDataItem!!.priceUsd / coinDataItem.priceUsd
            return currentCryptoAmount <= controlValue.withScale(coinDataItem.details.scale)
        }
        return true
    }

    private fun fetchEtherium() {
        getCoinByCodeUseCase(
            params = LocalCoinType.ETH.name,
            onSuccess = {
                etheriumCoinDataItem = it
                _initialLoadLiveData.value = LoadingData.Success(Unit)
            },
            onError = { _initialLoadLiveData.value = LoadingData.Error(it) }
        )
    }
}
