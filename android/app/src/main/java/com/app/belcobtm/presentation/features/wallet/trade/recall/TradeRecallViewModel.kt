package com.app.belcobtm.presentation.features.wallet.trade.recall

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.domain.transaction.interactor.trade.TradeRecallTransactionCompleteUseCase
import com.app.belcobtm.domain.transaction.interactor.trade.TradeRecallTransactionCreateUseCase
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.domain.wallet.item.CoinDataItem
import com.app.belcobtm.domain.wallet.item.CoinFeeDataItem
import com.app.belcobtm.presentation.core.item.CoinScreenItem
import com.app.belcobtm.presentation.core.item.mapToScreenItem
import com.app.belcobtm.presentation.core.mvvm.LoadingData

class TradeRecallViewModel(
    private val coinDataItem: CoinDataItem,
    private val feeDataItem: CoinFeeDataItem,
    private val createTransactionUseCase: TradeRecallTransactionCreateUseCase,
    private val completeTransactionUseCase: TradeRecallTransactionCompleteUseCase
) : ViewModel() {
    val createTransactionLiveData: MutableLiveData<LoadingData<Unit>> = MutableLiveData()
    val completeTransactionLiveData: MutableLiveData<LoadingData<Unit>> = MutableLiveData()
    val coinItem: CoinScreenItem = coinDataItem.mapToScreenItem()
    var selectedAmount: Double = 0.0

    fun createTransaction() {
        createTransactionLiveData.value = LoadingData.Loading()
        createTransactionUseCase.invoke(
            params = TradeRecallTransactionCreateUseCase.Params(coinDataItem.code, selectedAmount),
            onSuccess = { createTransactionLiveData.value = LoadingData.Success(Unit) },
            onError = { createTransactionLiveData.value = LoadingData.Error(it) }
        )
    }

    fun completeTransaction(smsCode: String) {
        completeTransactionLiveData.value = LoadingData.Loading()
        completeTransactionUseCase.invoke(
            params = TradeRecallTransactionCompleteUseCase.Params(smsCode, coinDataItem.code, selectedAmount),
            onSuccess = { completeTransactionLiveData.value = LoadingData.Success(Unit) },
            onError = { completeTransactionLiveData.value = LoadingData.Error(it) }
        )
    }

    fun getMaxValue(): Double = if (coinDataItem.code == LocalCoinType.CATM.name) {
        coinDataItem.balanceCoin
    } else {
        0.0.coerceAtLeast(coinDataItem.reservedBalanceCoin - feeDataItem.txFee)
    }

    fun isEnoughReservedAmount(): Boolean =
        coinDataItem.reservedBalanceCoin > feeDataItem.recallFee ?: feeDataItem.txFee
}