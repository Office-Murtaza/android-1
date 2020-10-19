package com.app.belcobtm.presentation.features.wallet.trade.reserve

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.domain.transaction.interactor.trade.TradeReserveTransactionCompleteUseCase
import com.app.belcobtm.domain.transaction.interactor.trade.TradeReserveTransactionCreateUseCase
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.domain.wallet.item.CoinDataItem
import com.app.belcobtm.domain.wallet.item.CoinDetailsDataItem
import com.app.belcobtm.presentation.core.item.CoinScreenItem
import com.app.belcobtm.presentation.core.item.mapToScreenItem
import com.app.belcobtm.presentation.core.mvvm.LoadingData

class TradeReserveViewModel(
    private val coinDataItem: CoinDataItem,
    private val detailsDataItem: CoinDetailsDataItem,
    private val createTransactionUseCase: TradeReserveTransactionCreateUseCase,
    private val completeTransactionUseCase: TradeReserveTransactionCompleteUseCase
) : ViewModel() {
    private var hash: String = ""
    val createTransactionLiveData: MutableLiveData<LoadingData<Unit>> = MutableLiveData()
    val completeTransactionLiveData: MutableLiveData<LoadingData<Unit>> = MutableLiveData()
    val coinItem: CoinScreenItem = coinDataItem.mapToScreenItem()
    var selectedAmount: Double = 0.0

    fun createTransaction() {
        createTransactionLiveData.value = LoadingData.Loading()
        createTransactionUseCase.invoke(
            params = TradeReserveTransactionCreateUseCase.Params(coinDataItem.code, selectedAmount),
            onSuccess = {
                hash = it
                createTransactionLiveData.value = LoadingData.Success(Unit)
            },
            onError = { createTransactionLiveData.value = LoadingData.Error(it) }
        )
    }

    fun completeTransaction(smsCode: String) {
        completeTransactionLiveData.value = LoadingData.Loading()
        completeTransactionUseCase.invoke(
            params = TradeReserveTransactionCompleteUseCase.Params(smsCode, coinDataItem.code, selectedAmount, hash),
            onSuccess = { completeTransactionLiveData.value = LoadingData.Success(Unit) },
            onError = { completeTransactionLiveData.value = LoadingData.Error(it) }
        )
    }

    fun getMaxValue(): Double = if (coinDataItem.code == LocalCoinType.CATM.name) {
        coinDataItem.balanceCoin
    } else {
        0.0.coerceAtLeast(coinDataItem.balanceCoin - detailsDataItem.txFee)
    }
}