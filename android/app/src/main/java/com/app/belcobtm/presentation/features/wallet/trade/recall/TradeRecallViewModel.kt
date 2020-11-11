package com.app.belcobtm.presentation.features.wallet.trade.recall

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.domain.transaction.interactor.trade.TradeRecallTransactionCompleteUseCase
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.domain.wallet.item.CoinDataItem
import com.app.belcobtm.domain.wallet.item.CoinDetailsDataItem
import com.app.belcobtm.presentation.core.extensions.withScale
import com.app.belcobtm.presentation.core.item.CoinScreenItem
import com.app.belcobtm.presentation.core.item.mapToScreenItem
import com.app.belcobtm.presentation.core.mvvm.LoadingData

class TradeRecallViewModel(
    private val coinDataItem: CoinDataItem,
    private val detailsDataItem: CoinDetailsDataItem,
    private val etheriumCoinDataItem: CoinDataItem,
    private val completeTransactionUseCase: TradeRecallTransactionCompleteUseCase
) : ViewModel() {
    val transactionLiveData: MutableLiveData<LoadingData<Unit>> = MutableLiveData()
    val coinItem: CoinScreenItem = coinDataItem.mapToScreenItem()
    var selectedAmount: Double = 0.0

    fun performTransaction() {
        transactionLiveData.value = LoadingData.Loading()
        completeTransactionUseCase.invoke(
            params = TradeRecallTransactionCompleteUseCase.Params(
                coinDataItem.code,
                selectedAmount
            ),
            onSuccess = { transactionLiveData.value = LoadingData.Success(Unit) },
            onError = { transactionLiveData.value = LoadingData.Error(it) }
        )
    }

    fun getMaxValue(): Double =
        0.0.coerceAtLeast(coinDataItem.reservedBalanceCoin - detailsDataItem.txFee)

    fun isEnoughRecallAmount(): Boolean = if (isCATM()) {
        val controlValue =
            detailsDataItem.txFee * etheriumCoinDataItem.priceUsd / coinDataItem.priceUsd
        selectedAmount <= controlValue.withScale(detailsDataItem.scale)
    } else {
        selectedAmount <= coinDataItem.reservedBalanceCoin - detailsDataItem.txFee
    }

    private fun isCATM(): Boolean {
        return coinDataItem.code == LocalCoinType.CATM.name
    }
}
