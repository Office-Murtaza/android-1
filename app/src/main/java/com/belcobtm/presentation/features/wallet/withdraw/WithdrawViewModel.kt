package com.belcobtm.presentation.features.wallet.withdraw

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.belcobtm.R
import com.belcobtm.domain.transaction.interactor.*
import com.belcobtm.domain.transaction.item.AmountItem
import com.belcobtm.domain.transaction.item.SignedTransactionPlanItem
import com.belcobtm.domain.transaction.item.TransactionPlanItem
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.domain.wallet.interactor.GetCoinListUseCase
import com.belcobtm.domain.wallet.item.CoinDataItem
import com.belcobtm.domain.wallet.item.isBtcCoin
import com.belcobtm.domain.wallet.item.isEthRelatedCoin
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.core.provider.string.StringProvider

class WithdrawViewModel(
    private val coinCode: String,
    private val getCoinListUseCase: GetCoinListUseCase,
    private val withdrawUseCase: WithdrawUseCase,
    private val getTransactionPlanUseCase: GetTransactionPlanUseCase,
    private val getSignedTransactionPlanUseCase: GetSignedTransactionPlanUseCase,
    private val getFakeSignedTransactionPlanUseCase: GetFakeSignedTransactionPlanUseCase,
    private val getMaxValueBySignedTransactionUseCase: GetMaxValueBySignedTransactionUseCase,
    private val stringProvider: StringProvider,
    private val receiverAccountActivatedUseCase: ReceiverAccountActivatedUseCase
) : ViewModel() {

    val transactionLiveData: MutableLiveData<LoadingData<Unit>> = MutableLiveData()

    private lateinit var fromCoinDataItem: CoinDataItem
    private lateinit var coinDataItemList: List<CoinDataItem>

    private val _loadingLiveData = MutableLiveData<LoadingData<Unit>>()
    val loadingLiveData: LiveData<LoadingData<Unit>>
        get() = _loadingLiveData

    private val _amount = MutableLiveData<AmountItem>()
    val amount: LiveData<AmountItem>
        get() = _amount

    private val _fee = MutableLiveData<Double>()
    val fee: LiveData<Double>
        get() = _fee

    private val _cryptoAmountError = MutableLiveData<String?>()
    val cryptoAmountError: LiveData<String?>
        get() = _cryptoAmountError

    private var transactionPlan: TransactionPlanItem? = null
    private var signedTransactionPlanItem: SignedTransactionPlanItem? = null

    init {
        fetchInitialData()
    }

    fun fetchInitialData() {
        _loadingLiveData.value = LoadingData.Loading()
        getCoinListUseCase.invoke(Unit, onSuccess = { coins ->
            coinDataItemList = coins
            fromCoinDataItem = coinDataItemList.find { it.code == coinCode }
                ?: throw IllegalStateException("Invalid coin code that is not presented in a list $coinCode")
            getTransactionPlanUseCase(
                fromCoinDataItem.code,
                onSuccess = { transactionPlan ->
                    this.transactionPlan = transactionPlan
                    getFakeSignedTransactionPlanUseCase(
                        GetFakeSignedTransactionPlanUseCase.Params(
                            fromCoinDataItem.code,
                            transactionPlan,
                            useMaxAmount = false
                        ),
                        onSuccess = { signedTransactionPlan ->
                            signedTransactionPlanItem = signedTransactionPlan
                            _fee.value = signedTransactionPlan.fee
                            _loadingLiveData.value = LoadingData.Success(Unit)
                        }, onError = {
                            _loadingLiveData.value = LoadingData.Error(it)
                        })
                }, onError = {
                    _loadingLiveData.value = LoadingData.Error(it)
                })
        }, onError = {
            _loadingLiveData.value = LoadingData.Error(it)
        })
    }

    fun withdraw(toAddress: String) {
        val coinAmount = _amount.value?.amount ?: 0.0
        val transactionPlan = transactionPlan ?: return
        _cryptoAmountError.value = null
        getSignedTransactionPlanUseCase(GetSignedTransactionPlanUseCase.Params(
            toAddress, fromCoinDataItem.code, coinAmount, transactionPlan
        ), onSuccess = { signedTransactionPlan ->
            _fee.value = signedTransactionPlan.fee
            signedTransactionPlanItem = signedTransactionPlan
            transactionLiveData.value = LoadingData.Loading()
            if (fromCoinDataItem.code == LocalCoinType.XRP.name) {
                if (coinAmount < 20) {
                    _cryptoAmountError.value =
                        stringProvider.getString(R.string.xrp_too_small_amount_error)
                    transactionLiveData.value = LoadingData.DismissProgress()
                    return@getSignedTransactionPlanUseCase
                } else {
                    receiverAccountActivatedUseCase(
                        ReceiverAccountActivatedUseCase.Params(toAddress, coinCode),
                        onSuccess = { activated ->
                            if (activated) {
                                withdrawInternal(coinAmount, toAddress, transactionPlan)
                            } else {
                                _cryptoAmountError.value =
                                    stringProvider.getString(R.string.xrp_too_small_amount_error)
                                transactionLiveData.value = LoadingData.DismissProgress()
                            }
                        },
                        onError = { transactionLiveData.value = LoadingData.Error(it) }
                    )
                }
            } else {
                withdrawInternal(coinAmount, toAddress, transactionPlan)
            }
        }, onError = {
            transactionLiveData.value = LoadingData.Error(it)
        })
    }

    private fun withdrawInternal(
        coinAmount: Double,
        toAddress: String,
        transactionPlan: TransactionPlanItem
    ) {
        withdrawUseCase.invoke(
            params = WithdrawUseCase.Params(
                getCoinCode(),
                coinAmount,
                toAddress,
                _fee.value ?: 0.0,
                transactionPlan
            ),
            onSuccess = { transactionLiveData.value = LoadingData.Success(it) },
            onError = { transactionLiveData.value = LoadingData.Error(it) }
        )
    }

    fun setAmount(amount: Double) {
        if (_amount.value?.useMax == true) {
            transactionPlan?.let { plan ->
                getFakeSignedTransactionPlanUseCase(
                    GetFakeSignedTransactionPlanUseCase.Params(
                        fromCoinDataItem.code, plan, useMaxAmount = false, amount = amount
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
        val transactionPlan = transactionPlan ?: return
        if (_amount.value?.useMax == true) {
            return
        }
        getMaxValueBySignedTransactionUseCase(
            GetMaxValueBySignedTransactionUseCase.Params(
                transactionPlan,
                fromCoinDataItem,
            ),
            onSuccess = {
                _fee.value = it.fee
                _amount.value = AmountItem(it.amount, useMax = true)
            }, onError = { /* error impossible */ }
        )
    }

    fun getCoinBalance(): Double = fromCoinDataItem.balanceCoin

    fun getUsdBalance(): Double = fromCoinDataItem.balanceUsd

    fun getUsdPrice(): Double = fromCoinDataItem.priceUsd

    fun getReservedBalanceUsd(): Double = fromCoinDataItem.reservedBalanceUsd

    fun getReservedBalanceCoin(): Double = fromCoinDataItem.reservedBalanceCoin

    fun getCoinCode(): String = coinCode

    fun isSufficientBalance(): Boolean {
        val amount = _amount.value?.amount ?: 0.0
        val fee = _fee.value ?: 0.0
        return if (fromCoinDataItem.isBtcCoin()) {
            amount + fee <= signedTransactionPlanItem?.availableAmount ?: 0.0
        } else {
            amount <= fromCoinDataItem.balanceCoin
        }
    }

    fun isSufficientEth(): Boolean {
        val ethBalance = coinDataItemList.firstOrNull {
            it.code == LocalCoinType.ETH.name
        }?.balanceCoin ?: 0.0
        return !fromCoinDataItem.isEthRelatedCoin() || ethBalance >= (transactionPlan?.txFee ?: 0.0)
    }
}