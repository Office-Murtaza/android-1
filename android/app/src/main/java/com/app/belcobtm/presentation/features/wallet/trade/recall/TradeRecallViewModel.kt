package com.app.belcobtm.presentation.features.wallet.trade.recall

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.domain.transaction.interactor.trade.TradeRecallTransactionCompleteUseCase
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.domain.wallet.interactor.GetFreshCoinUseCase
import com.app.belcobtm.domain.wallet.item.CoinDataItem
import com.app.belcobtm.domain.wallet.item.CoinDetailsDataItem
import com.app.belcobtm.presentation.core.extensions.withScale
import com.app.belcobtm.presentation.core.item.CoinScreenItem
import com.app.belcobtm.presentation.core.item.mapToScreenItem
import com.app.belcobtm.presentation.core.mvvm.LoadingData

class TradeRecallViewModel(
    private val coinDataItem: CoinDataItem,
    private val detailsDataItem: CoinDetailsDataItem,
    private val getCoinDataUseCase: GetFreshCoinUseCase,
    private val completeTransactionUseCase: TradeRecallTransactionCompleteUseCase
) : ViewModel() {
    private val _initialLoadLiveData = MutableLiveData<LoadingData<Unit>>()
    val initialLoadLiveData: LiveData<LoadingData<Unit>> = _initialLoadLiveData

    private val _transactionLiveData = MutableLiveData<LoadingData<Unit>>()
    val transactionLiveData: LiveData<LoadingData<Unit>> = _transactionLiveData

    private var etheriumCoinDataItem: CoinDataItem? = null
    val coinItem: CoinScreenItem = coinDataItem.mapToScreenItem()
    var selectedAmount: Double = 0.0

    init {
        if (isCATM()) {
            // for CATM amount calculation we need ETH coin
            fetchEtherium()
        }
    }

    fun performTransaction() {
        _transactionLiveData.value = LoadingData.Loading()
        completeTransactionUseCase.invoke(
            params = TradeRecallTransactionCompleteUseCase.Params(
                coinDataItem.code,
                selectedAmount
            ),
            onSuccess = { _transactionLiveData.value = LoadingData.Success(Unit) },
            onError = { _transactionLiveData.value = LoadingData.Error(it) }
        )
    }

    fun getMaxValue(): Double = when {
        isXRP() -> 0.0.coerceAtLeast(coinDataItem.reservedBalanceCoin - detailsDataItem.txFee - 20)
        else -> 0.0.coerceAtLeast(coinDataItem.reservedBalanceCoin - detailsDataItem.txFee)
    }

    fun isEnoughRecallAmount(): Boolean {
        return if (isCATM()) {
            val localEtheriumItem = etheriumCoinDataItem ?: return false
            val controlValue =
                detailsDataItem.txFee * localEtheriumItem.priceUsd / coinDataItem.priceUsd
            selectedAmount <= controlValue.withScale(detailsDataItem.scale)
        } else {
            selectedAmount <= getMaxValue()
        }
    }

    private fun isCATM(): Boolean {
        return coinDataItem.code == LocalCoinType.CATM.name
    }

    private fun isXRP(): Boolean {
        return coinDataItem.code == LocalCoinType.XRP.name
    }

    private fun fetchEtherium() {
        _initialLoadLiveData.value = LoadingData.Loading()
        getCoinDataUseCase.invoke(
            params = GetFreshCoinUseCase.Params(LocalCoinType.ETH.name),
            onSuccess = {
                etheriumCoinDataItem = it
                _initialLoadLiveData.value = LoadingData.Success(Unit)
            },
            onError = { _initialLoadLiveData.value = LoadingData.Error(it) }
        )
    }
}
