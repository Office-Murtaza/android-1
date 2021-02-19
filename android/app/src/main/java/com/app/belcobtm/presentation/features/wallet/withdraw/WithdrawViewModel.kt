package com.app.belcobtm.presentation.features.wallet.withdraw

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.belcobtm.domain.transaction.interactor.WithdrawUseCase
import com.app.belcobtm.domain.wallet.interactor.GetCoinDetailsUseCase
import com.app.belcobtm.domain.wallet.interactor.GetCoinListUseCase
import com.app.belcobtm.domain.wallet.item.CoinDataItem
import com.app.belcobtm.domain.wallet.item.CoinDetailsDataItem
import com.app.belcobtm.presentation.core.coin.AmountCoinValidator
import com.app.belcobtm.presentation.core.coin.MinMaxCoinValueProvider
import com.app.belcobtm.presentation.core.coin.model.ValidationResult
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class WithdrawViewModel(
    private val coinCode: String,
    private val getCoinListUseCase: GetCoinListUseCase,
    getCoinDetailsUseCase: GetCoinDetailsUseCase,
    private val withdrawUseCase: WithdrawUseCase,
    private val minMaxCoinValueProvider: MinMaxCoinValueProvider,
    private val amountCoinValidator: AmountCoinValidator
) : ViewModel() {

    val transactionLiveData: MutableLiveData<LoadingData<Unit>> = MutableLiveData()

    private lateinit var fromCoinDataItem: CoinDataItem
    private lateinit var coinDataItemList: List<CoinDataItem>
    private lateinit var fromCoinDetailsDataItem: CoinDetailsDataItem

    private val _loadingLiveData = MutableLiveData<LoadingData<Unit>>()
    val loadingLiveData: LiveData<LoadingData<Unit>>
        get() = _loadingLiveData

    init {
        _loadingLiveData.value = LoadingData.Loading()
        getCoinDetailsUseCase.invoke(
            GetCoinDetailsUseCase.Params(coinCode),
            onSuccess = { coinDetails ->
                getCoinListUseCase.invoke(Unit, onSuccess = {
                    coinDataItemList = it
                    fromCoinDataItem = coinDataItemList.find { it.code == coinCode }
                        ?: throw IllegalStateException("Invalid coin code that is not presented in a list $coinCode")
                    fromCoinDetailsDataItem = coinDetails
                    _loadingLiveData.value = LoadingData.Success(Unit)
                }, onError = {
                    _loadingLiveData.value = LoadingData.Error(it)
                })
            },
            onError = {
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

    fun getMinValue(): Double =
        minMaxCoinValueProvider.getMinValue(fromCoinDataItem, fromCoinDetailsDataItem)

    fun getMaxValue(): Double =
        minMaxCoinValueProvider.getMaxValue(fromCoinDataItem, fromCoinDetailsDataItem)

    fun getTransactionFee(): Double = fromCoinDetailsDataItem.txFee

    fun getCoinBalance(): Double = fromCoinDataItem.balanceCoin

    fun getUsdBalance(): Double = fromCoinDataItem.balanceUsd

    fun getUsdPrice(): Double = fromCoinDataItem.priceUsd

    fun getReservedBalanceUsd(): Double = fromCoinDataItem.reservedBalanceUsd

    fun getReservedBalanceCoin(): Double = fromCoinDataItem.reservedBalanceCoin

    fun getCoinCode(): String = coinCode

    fun validateAmount(amount: Double): ValidationResult =
        amountCoinValidator.validateBalance(
            amount, fromCoinDataItem, fromCoinDetailsDataItem, coinDataItemList
        )
}