package com.app.belcobtm.presentation.features.wallet.trade.reserve

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.domain.transaction.interactor.trade.TradeReserveTransactionCompleteUseCase
import com.app.belcobtm.domain.transaction.interactor.trade.TradeReserveTransactionCreateUseCase
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.domain.wallet.interactor.GetFreshCoinUseCase
import com.app.belcobtm.domain.wallet.item.CoinDataItem
import com.app.belcobtm.domain.wallet.item.CoinDetailsDataItem
import com.app.belcobtm.presentation.core.item.CoinScreenItem
import com.app.belcobtm.presentation.core.item.mapToScreenItem
import com.app.belcobtm.presentation.core.mvvm.LoadingData

class TradeReserveViewModel(
    private val coinDataItem: CoinDataItem,
    private val detailsDataItem: CoinDetailsDataItem,
    private val getCoinUseCace: GetFreshCoinUseCase,
    private val createTransactionUseCase: TradeReserveTransactionCreateUseCase,
    private val completeTransactionUseCase: TradeReserveTransactionCompleteUseCase
) : ViewModel() {
    private val _initialLoadLiveData = MutableLiveData<LoadingData<Unit>>()
    val initialLoadLiveData: LiveData<LoadingData<Unit>> = _initialLoadLiveData

    private val _createTransactionLiveData = MutableLiveData<LoadingData<Unit>>()
    val createTransactionLiveData: LiveData<LoadingData<Unit>> = _createTransactionLiveData

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

    fun createTransaction() {
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
            onSuccess = { _createTransactionLiveData.value = LoadingData.Success(Unit) },
            onError = { _createTransactionLiveData.value = LoadingData.Error(it) }
        )
    }

    fun getMaxValue(): Double = when {
        isCATM() -> coinDataItem.balanceCoin
        // at least 20 XRP coins must stay within the wallet
        isXRP() -> 0.0.coerceAtLeast(coinDataItem.balanceCoin - getTransactionFee() - 20)
        else -> 0.0.coerceAtLeast(coinDataItem.balanceCoin - getTransactionFee())
    }

    private fun getMinValue(): Double {
        return getTransactionFee()
    }

    private fun getTransactionFee(): Double = when  {
        else -> detailsDataItem.txFee
    }

    fun validateCryptoAmount(amount: Double) {
        selectedAmount = amount

        val minValue = getMinValue()
        val maxValue = getMaxValue()
        val enoughETHForExtraFee = enoughETHForExtraFee()
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

    private fun enoughETHForExtraFee(): Boolean {
        if (isCATM()) {
            return etheriumCoinDataItem!!.balanceCoin >= detailsDataItem.txFee
        }
        return true
    }

    private fun isCATM(): Boolean {
        return coinDataItem.code == LocalCoinType.CATM.name
    }

    private fun isXRP(): Boolean {
        return coinDataItem.code == LocalCoinType.XRP.name
    }

    private fun fetchEtherium() {
        _initialLoadLiveData.value = LoadingData.Loading()
        getCoinUseCace.invoke(
            params = GetFreshCoinUseCase.Params(LocalCoinType.ETH.name),
            onSuccess = {
                etheriumCoinDataItem = it
                _initialLoadLiveData.value = LoadingData.Success(Unit)
            },
            onError = { _initialLoadLiveData.value = LoadingData.Error(it) }
        )
    }
}
