package com.belcobtm.presentation.features.wallet.trade.reserve

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belcobtm.domain.transaction.interactor.GetFakeSignedTransactionPlanUseCase
import com.belcobtm.domain.transaction.interactor.GetMaxValueBySignedTransactionUseCase
import com.belcobtm.domain.transaction.interactor.GetSignedTransactionPlanUseCase
import com.belcobtm.domain.transaction.interactor.GetTransactionPlanUseCase
import com.belcobtm.domain.transaction.interactor.trade.TradeReserveTransactionCompleteUseCase
import com.belcobtm.domain.transaction.interactor.trade.TradeReserveTransactionCreateUseCase
import com.belcobtm.domain.transaction.item.AmountItem
import com.belcobtm.domain.transaction.item.SignedTransactionPlanItem
import com.belcobtm.domain.transaction.item.TransactionPlanItem
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.domain.wallet.interactor.GetCoinByCodeUseCase
import com.belcobtm.domain.wallet.item.CoinDataItem
import com.belcobtm.domain.wallet.item.isEthRelatedCoin
import com.belcobtm.presentation.core.coin.CoinCodeProvider
import com.belcobtm.presentation.core.item.CoinScreenItem
import com.belcobtm.presentation.core.item.mapToScreenItem
import com.belcobtm.presentation.core.mvvm.LoadingData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TradeReserveViewModel(
    private val coinCode: String,
    private val getCoinByCodeUseCase: GetCoinByCodeUseCase,
    private val createTransactionUseCase: TradeReserveTransactionCreateUseCase,
    private val completeTransactionUseCase: TradeReserveTransactionCompleteUseCase,
    private val coinCodeProvider: CoinCodeProvider,
    private val getTransactionPlanUseCase: GetTransactionPlanUseCase,
    private val getSignedTransactionPlanUseCase: GetSignedTransactionPlanUseCase,
    private val getFakeSignedTransactionPlanUseCase: GetFakeSignedTransactionPlanUseCase,
    private val getMaxValueBySignedTransactionUseCase: GetMaxValueBySignedTransactionUseCase
) : ViewModel() {

    private var transactionPlanItem: TransactionPlanItem? = null
    private var signedTransactionPlanItem: SignedTransactionPlanItem? = null

    private val _initialLoadLiveData = MutableLiveData<LoadingData<Unit>>()
    val initialLoadLiveData: LiveData<LoadingData<Unit>>
        get() = _initialLoadLiveData

    private val _createTransactionLiveData = MutableLiveData<LoadingData<Unit>>()
    val createTransactionLiveData: LiveData<LoadingData<Unit>>
        get() = _createTransactionLiveData

    private val _cryptoFieldState = MutableLiveData<InputFieldState>()
    val cryptoFieldState: LiveData<InputFieldState>
        get() = _cryptoFieldState

    private val _amount = MutableLiveData<AmountItem>()
    val amount: LiveData<AmountItem>
        get() = _amount

    private val _fee = MutableLiveData<Double>()
    val fee: LiveData<Double>
        get() = _fee

    private var etheriumCoinDataItem: CoinDataItem? = null
    private lateinit var coinDataItem: CoinDataItem
    lateinit var coinItem: CoinScreenItem
        private set

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
                getFakeSignedTransactionPlanUseCase(
                    GetFakeSignedTransactionPlanUseCase.Params(
                        coinCode,
                        transactionPlan,
                        useMaxAmount = false
                    ),
                    onSuccess = { signedTransactionPlan ->
                        signedTransactionPlanItem = signedTransactionPlan
                        _fee.value = signedTransactionPlan.fee
                        if (coinItem.isEthRelatedCoin()) {
                            // for CATM amount calculation we need ETH coin
                            fetchEtherium()
                        } else {
                            _initialLoadLiveData.value = LoadingData.Success(Unit)
                        }
                    }, onError = {
                        _initialLoadLiveData.value = LoadingData.Error(it)
                    })
            }, onError = {
                _initialLoadLiveData.value = LoadingData.Error(it)
            })
        }, onError = {
            _initialLoadLiveData.value = LoadingData.Error(it)
        })
    }

    fun createTransaction() {
        val transactionPlanItem = transactionPlanItem ?: return
        val cryptoAmount = _amount.value?.amount ?: 0.0
        if (!validateCryptoAmount()) {
            return
        }
        getSignedTransactionPlanUseCase(GetSignedTransactionPlanUseCase.Params(
            coinDataItem.details.walletAddress, coinCode, cryptoAmount, transactionPlanItem
        ), onSuccess = {
            _fee.value = it.fee
            signedTransactionPlanItem = it
            _createTransactionLiveData.value = LoadingData.Loading()
            createTransactionUseCase.invoke(
                params = TradeReserveTransactionCreateUseCase.Params(
                    coinDataItem.code, cryptoAmount, transactionPlanItem
                ),
                onSuccess = { completeTransaction(it) },
                onError = { _createTransactionLiveData.value = LoadingData.Error(it) }
            )
        }, onError = {
            _createTransactionLiveData.value = LoadingData.Error(it)
        })
    }

    private fun completeTransaction(hash: String) {
        val transactionPlanItem = transactionPlanItem ?: return
        _createTransactionLiveData.value = LoadingData.Loading()
        completeTransactionUseCase.invoke(
            params = TradeReserveTransactionCompleteUseCase.Params(
                coinDataItem.code,
                _amount.value?.amount ?: 0.0,
                hash,
                _fee.value ?: 0.0,
                transactionPlanItem
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

    fun getCoinCode(): String = coinCodeProvider.getCoinCode(coinDataItem)

    fun setAmount(amount: Double) {
        if (_amount.value?.useMax == true) {
            transactionPlanItem?.let { plan ->
                getFakeSignedTransactionPlanUseCase(
                    GetFakeSignedTransactionPlanUseCase.Params(
                        coinDataItem.code, plan, useMaxAmount = false, amount = amount
                    ),
                    onSuccess = { signedTransactionPlan ->
                        signedTransactionPlanItem = signedTransactionPlan
                        _fee.value = signedTransactionPlan.fee
                    },
                    onError = { /* Failure impossible */ }
                )
            }
        }
        _amount.value = AmountItem(amount, useMax = false)
    }

    fun setMaxAmount() {
        val transactionPlanItem = transactionPlanItem ?: return
        if (_amount.value?.useMax == true) {
            return
        }
        getMaxValueBySignedTransactionUseCase(
            GetMaxValueBySignedTransactionUseCase.Params(
                transactionPlanItem,
                coinDataItem,
            ),
            onSuccess = {
                _fee.value = it.fee
                _amount.value = AmountItem(it.amount, useMax = true)
            }, onError = { /* error impossible */ }
        )
    }

    private fun validateCryptoAmount(): Boolean {
        val enoughETHForExtraFee = isSufficientEth()
        val selectedAmount = _amount.value?.amount ?: 0.0
        when {
            !isSufficientBalance() ->
                _cryptoFieldState.value = InputFieldState.MoreThanNeedError
            selectedAmount <= 0 ->
                _cryptoFieldState.value = InputFieldState.LessThanNeedError
            enoughETHForExtraFee.not() ->
                _cryptoFieldState.value = InputFieldState.NotEnoughETHError
            isSufficientBalance() && enoughETHForExtraFee -> {
                _cryptoFieldState.value = InputFieldState.Valid
                return true
            }
        }
        return false
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

    private fun isSufficientBalance(): Boolean {
        val amount = _amount.value?.amount ?: 0.0
        val fee = _fee.value ?: 0.0
        return if (!coinDataItem.isEthRelatedCoin()) {
            amount + fee < signedTransactionPlanItem?.availableAmount ?: 0.0
        } else {
            amount < coinDataItem.reservedBalanceCoin
        }
    }

    private fun isSufficientEth(): Boolean {
        val ethBalance = etheriumCoinDataItem?.balanceCoin ?: 0.0
        return !coinDataItem.isEthRelatedCoin() || ethBalance >= (transactionPlanItem?.txFee ?: 0.0)
    }
}
