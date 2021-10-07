package com.belcobtm.presentation.features.wallet.withdraw

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.belcobtm.domain.transaction.interactor.GetFakeFeeUseCase
import com.belcobtm.domain.transaction.interactor.GetFeeUseCase
import com.belcobtm.domain.transaction.interactor.GetTransactionPlanUseCase
import com.belcobtm.domain.transaction.interactor.WithdrawUseCase
import com.belcobtm.domain.transaction.item.TransactionPlanItem
import com.belcobtm.domain.wallet.interactor.GetCoinListUseCase
import com.belcobtm.domain.wallet.item.CoinDataItem
import com.belcobtm.presentation.core.coin.AmountCoinValidator
import com.belcobtm.presentation.core.coin.CoinLimitsValueProvider
import com.belcobtm.presentation.core.coin.model.ValidationResult
import com.belcobtm.presentation.core.mvvm.LoadingData

class WithdrawViewModel(
    private val coinCode: String,
    private val getCoinListUseCase: GetCoinListUseCase,
    private val withdrawUseCase: WithdrawUseCase,
    private val coinLimitsValueProvider: CoinLimitsValueProvider,
    private val amountCoinValidator: AmountCoinValidator,
    private val getTransactionPlanUseCase: GetTransactionPlanUseCase,
    private val getFeeUseCase: GetFeeUseCase,
    private val getFakeFeeUseCase: GetFakeFeeUseCase
) : ViewModel() {

    val transactionLiveData: MutableLiveData<LoadingData<Unit>> = MutableLiveData()

    private lateinit var fromCoinDataItem: CoinDataItem
    private lateinit var coinDataItemList: List<CoinDataItem>

    private val _loadingLiveData = MutableLiveData<LoadingData<Unit>>()
    val loadingLiveData: LiveData<LoadingData<Unit>>
        get() = _loadingLiveData

    private val _fee = MutableLiveData<Double>()
    val fee: LiveData<Double>
        get() = _fee

    private var transactionPlan: TransactionPlanItem? = null

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
                    getFakeFeeUseCase(
                        GetFakeFeeUseCase.Params(fromCoinDataItem.code, transactionPlan),
                        onSuccess = { fee ->
                            _fee.value = fee
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

    fun withdraw(
        toAddress: String,
        coinAmount: Double
    ) {
        val transactionPlan = transactionPlan ?: return
        getFeeUseCase(GetFeeUseCase.Params(
            toAddress, fromCoinDataItem.code, coinAmount, transactionPlan
        ), onSuccess = {
            transactionLiveData.value = LoadingData.Loading()
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
        }, onError = {
            transactionLiveData.value = LoadingData.Error(it)
        })
    }

    fun getMaxValue(): Double =
        coinLimitsValueProvider.getMaxValue(fromCoinDataItem, _fee.value ?: 0.0)

    fun getCoinBalance(): Double = fromCoinDataItem.balanceCoin

    fun getUsdBalance(): Double = fromCoinDataItem.balanceUsd

    fun getUsdPrice(): Double = fromCoinDataItem.priceUsd

    fun getReservedBalanceUsd(): Double = fromCoinDataItem.reservedBalanceUsd

    fun getReservedBalanceCoin(): Double = fromCoinDataItem.reservedBalanceCoin

    fun getCoinCode(): String = coinCode

    fun validateAmount(amount: Double): ValidationResult =
        amountCoinValidator.validateBalance(
            amount, fromCoinDataItem, coinDataItemList
        )
}