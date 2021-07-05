package com.belcobtm.presentation.features.wallet.withdraw

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belcobtm.domain.transaction.interactor.WithdrawUseCase
import com.belcobtm.domain.wallet.interactor.GetCoinListUseCase
import com.belcobtm.domain.wallet.item.CoinDataItem
import com.belcobtm.presentation.core.coin.AmountCoinValidator
import com.belcobtm.presentation.core.coin.CoinLimitsValueProvider
import com.belcobtm.presentation.core.coin.model.ValidationResult
import com.belcobtm.presentation.core.mvvm.LoadingData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class WithdrawViewModel(
    private val coinCode: String,
    getCoinListUseCase: GetCoinListUseCase,
    private val withdrawUseCase: WithdrawUseCase,
    private val coinLimitsValueProvider: CoinLimitsValueProvider,
    private val amountCoinValidator: AmountCoinValidator
) : ViewModel() {

    val transactionLiveData: MutableLiveData<LoadingData<Unit>> = MutableLiveData()

    private lateinit var fromCoinDataItem: CoinDataItem
    private lateinit var coinDataItemList: List<CoinDataItem>

    private val _loadingLiveData = MutableLiveData<LoadingData<Unit>>()
    val loadingLiveData: LiveData<LoadingData<Unit>>
        get() = _loadingLiveData

    init {
        _loadingLiveData.value = LoadingData.Loading()
        getCoinListUseCase.invoke(Unit, onSuccess = {
            coinDataItemList = it
            fromCoinDataItem = coinDataItemList.find { it.code == coinCode }
                ?: throw IllegalStateException("Invalid coin code that is not presented in a list $coinCode")
            _loadingLiveData.value = LoadingData.Success(Unit)
        }, onError = {
            _loadingLiveData.value = LoadingData.Error(it)
        })
    }

    fun withdraw(
        toAddress: String,
        coinAmount: Double
    ) {
        transactionLiveData.value = LoadingData.Loading()
        withdrawUseCase.invoke(
            params = WithdrawUseCase.Params(getCoinCode(), coinAmount, toAddress),
            onSuccess = {
                // we need to add some delay as server returns 200 before writting to DB
                viewModelScope.launch {
                    delay(1000)
                    transactionLiveData.value = LoadingData.Success(it)
                }
            },
            onError = { transactionLiveData.value = LoadingData.Error(it) }
        )
    }

    fun getMaxValue(): Double =
        coinLimitsValueProvider.getMaxValue(fromCoinDataItem)

    fun getTransactionFee(): Double = fromCoinDataItem.details.txFee

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