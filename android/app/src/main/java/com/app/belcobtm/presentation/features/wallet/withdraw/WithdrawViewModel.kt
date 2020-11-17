package com.app.belcobtm.presentation.features.wallet.withdraw

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.domain.transaction.interactor.WithdrawUseCase
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.domain.wallet.item.CoinDataItem
import com.app.belcobtm.domain.wallet.item.CoinDetailsDataItem
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import kotlin.math.max

class WithdrawViewModel(
    private val withdrawUseCase: WithdrawUseCase,
    private val fromCoinDataItem: CoinDataItem?,
    private val fromCoinDetailsDataItem: CoinDetailsDataItem,
    private val coinDataItemList: List<CoinDataItem>
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

    fun getMinValue(): Double = getTransactionFee()

    fun getMaxValue(): Double = when (getCoinCode()) {
        LocalCoinType.CATM.name -> getCoinBalance()
        LocalCoinType.XRP.name -> max(0.0, getCoinBalance() - getTransactionFee() - 20)
        else -> max(0.0, getCoinBalance() - getTransactionFee())
    }

    fun getTransactionFee(): Double = fromCoinDetailsDataItem.txFee

    fun getCoinBalance(): Double = fromCoinDataItem?.balanceCoin ?: 0.0

    fun getUsdBalance(): Double = fromCoinDataItem?.balanceUsd ?: 0.0

    fun getUsdPrice(): Double = fromCoinDataItem?.priceUsd ?: 0.0

    fun getReservedBalanceUsd(): Double = fromCoinDataItem?.reservedBalanceUsd ?: 0.0

    fun getReservedBalanceCoin(): Double = fromCoinDataItem?.reservedBalanceCoin ?: 0.0

    fun getCoinCode(): String = fromCoinDataItem?.code ?: ""

    fun isNotEnoughBalanceETH(): Boolean =
        coinDataItemList.find { LocalCoinType.ETH.name == it.code }?.balanceCoin ?: 0.0 < getTransactionFee()
}