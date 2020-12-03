package com.app.belcobtm.presentation.features.wallet.trade.recall

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.domain.transaction.interactor.trade.TradeRecallTransactionCompleteUseCase
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.domain.wallet.interactor.GetFreshCoinUseCase
import com.app.belcobtm.domain.wallet.item.CoinDataItem
import com.app.belcobtm.domain.wallet.item.CoinDetailsDataItem
import com.app.belcobtm.presentation.core.extensions.withScale
import com.app.belcobtm.presentation.core.item.CoinScreenItem
import com.app.belcobtm.presentation.core.item.mapToScreenItem
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.features.wallet.trade.reserve.InputFieldState

class TradeRecallViewModel(
    private val coinDataItem: CoinDataItem,
    private val detailsDataItem: CoinDetailsDataItem,
    private val getCoinDataUseCase: GetFreshCoinUseCase,
    private val completeTransactionUseCase: TradeRecallTransactionCompleteUseCase
) : ViewModel() {
    private val _initialLoadLiveData = MutableLiveData<LoadingData<Unit>>()
    val initialLoadLiveData: LiveData<LoadingData<Unit>> = _initialLoadLiveData

    private val _transactionLiveData = MutableLiveData<LoadingData<Unit>>()
    val transactionLiveData: LiveData<LoadingData<Unit>> = _transactionLiveData

    private val _cryptoFieldState = MutableLiveData<InputFieldState>()
    val cryptoFieldState: LiveData<InputFieldState> = _cryptoFieldState

    private val _usdFieldState = MutableLiveData<InputFieldState>()
    val usdFieldState: LiveData<InputFieldState> = _usdFieldState

    private val _submitButtonEnable = MutableLiveData(false)
    val submitButtonEnable: LiveData<Boolean> = _submitButtonEnable

    private var etheriumCoinDataItem: CoinDataItem? = null
    val coinItem: CoinScreenItem = coinDataItem.mapToScreenItem()

    private var selectedAmount: Double = 0.0

    init {
        if (isCATM()) {
            // for CATM amount calculation we need ETH coin
            fetchEtherium()
        }
    }

    fun performTransaction() {
        _transactionLiveData.value = LoadingData.Loading()
        completeTransactionUseCase.invoke(
            params = TradeRecallTransactionCompleteUseCase.Params(
                coinDataItem.code,
                selectedAmount
            ),
            onSuccess = { _transactionLiveData.value = LoadingData.Success(Unit) },
            onError = { _transactionLiveData.value = LoadingData.Error(it) }
        )
    }

    fun validateCryptoAmount(amount: Double) {
        selectedAmount = amount

        val minValue = getMinValue()
        val maxValue = getMaxValue()
        val enoughETHForExtraFee = enoughETHForExtraFee(amount)
        when {
            amount in minValue..maxValue && enoughETHForExtraFee -> {
                _cryptoFieldState.value = InputFieldState.Valid
                _usdFieldState.value = InputFieldState.Valid
                _submitButtonEnable.value = true
            }
            amount > maxValue -> {
                _cryptoFieldState.value = InputFieldState.MoreThanNeedError
                _usdFieldState.value = InputFieldState.MoreThanNeedError
                _submitButtonEnable.value = false
            }
            amount < minValue -> {
                _cryptoFieldState.value = InputFieldState.LessThanNeedError
                _usdFieldState.value = InputFieldState.LessThanNeedError
                _submitButtonEnable.value = false
            }
            enoughETHForExtraFee.not() -> {
                _cryptoFieldState.value = InputFieldState.NotEnoughETHError
                _usdFieldState.value = InputFieldState.NotEnoughETHError
                _submitButtonEnable.value = false
            }
        }
    }

    // TODO wrong max value handling
    fun getMaxValue(): Double =
        0.0.coerceAtLeast(coinDataItem.reservedBalanceCoin - getTransactionFee())

    private fun enoughETHForExtraFee(currentCryptoAmount: Double): Boolean {
        if (isCATM()) {
            val controlValue =
                detailsDataItem.txFee * etheriumCoinDataItem!!.priceUsd / coinDataItem.priceUsd
            return currentCryptoAmount <= controlValue.withScale(detailsDataItem.scale)
        }
        return true
    }

    private fun getMinValue(): Double {
        return getTransactionFee()
    }

    private fun getTransactionFee(): Double = detailsDataItem.txFee

    private fun isCATM(): Boolean {
        return coinDataItem.code == LocalCoinType.CATM.name
    }

    private fun fetchEtherium() {
        _initialLoadLiveData.value = LoadingData.Loading()
        getCoinDataUseCase.invoke(
            params = GetFreshCoinUseCase.Params(LocalCoinType.ETH.name),
            onSuccess = {
                etheriumCoinDataItem = it
                _initialLoadLiveData.value = LoadingData.Success(Unit)
            },
            onError = { _initialLoadLiveData.value = LoadingData.Error(it) }
        )
    }
}
