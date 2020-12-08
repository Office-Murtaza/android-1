package com.app.belcobtm.presentation.features.wallet.withdraw

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.domain.transaction.interactor.WithdrawUseCase
import com.app.belcobtm.domain.wallet.item.CoinDataItem
import com.app.belcobtm.domain.wallet.item.CoinDetailsDataItem
import com.app.belcobtm.presentation.core.coin.AmountCoinValidator
import com.app.belcobtm.presentation.core.coin.CoinCodeProvider
import com.app.belcobtm.presentation.core.coin.MinMaxCoinValueProvider
import com.app.belcobtm.presentation.core.coin.model.ValidationResult
import com.app.belcobtm.presentation.core.mvvm.LoadingData

class WithdrawViewModel(
    private val withdrawUseCase: WithdrawUseCase,
    private val fromCoinDataItem: CoinDataItem?,
    private val fromCoinDetailsDataItem: CoinDetailsDataItem,
    private val coinDataItemList: List<CoinDataItem>,
    private val minMaxCoinValueProvider: MinMaxCoinValueProvider,
    private val coinCodeProvider: CoinCodeProvider,
    private val amountCoinValidator: AmountCoinValidator
) : ViewModel() {

    val transactionLiveData: MutableLiveData<LoadingData<Unit>> = MutableLiveData()

    fun withdraw(
        toAddress: String,
        coinAmount: Double
    ) {
        transactionLiveData.value = LoadingData.Loading()
        withdrawUseCase.invoke(
            params = WithdrawUseCase.Params(fromCoinDataItem?.code ?: "", coinAmount, toAddress),
            onSuccess = { transactionLiveData.value = LoadingData.Success(it) },
            onError = { transactionLiveData.value = LoadingData.Error(it) }
        )
    }

    fun getMinValue(): Double =
        fromCoinDataItem?.let {
            minMaxCoinValueProvider.getMinValue(it, fromCoinDetailsDataItem)
        } ?: 0.0

    fun getMaxValue(): Double = fromCoinDataItem?.let {
        minMaxCoinValueProvider.getMaxValue(it, fromCoinDetailsDataItem)
    } ?: 0.0

    fun getTransactionFee(): Double = fromCoinDetailsDataItem.txFee

    fun getCoinBalance(): Double = fromCoinDataItem?.balanceCoin ?: 0.0

    fun getUsdBalance(): Double = fromCoinDataItem?.balanceUsd ?: 0.0

    fun getUsdPrice(): Double = fromCoinDataItem?.priceUsd ?: 0.0

    fun getReservedBalanceUsd(): Double = fromCoinDataItem?.reservedBalanceUsd ?: 0.0

    fun getReservedBalanceCoin(): Double = fromCoinDataItem?.reservedBalanceCoin ?: 0.0

    fun getCoinCode(): String = fromCoinDataItem?.let(coinCodeProvider::getCoinCode) ?: ""

    fun validateAmount(amount: Double): ValidationResult =
        amountCoinValidator.validateBalance(
            amount, fromCoinDataItem, fromCoinDetailsDataItem, coinDataItemList
        )
}