package com.belcobtm.presentation.screens.wallet.trade.mytrade.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.trade.details.DeleteTradeUseCase
import com.belcobtm.domain.trade.list.ObserveMyTradesUseCase
import com.belcobtm.presentation.core.adapter.model.ListItem
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.screens.wallet.trade.list.model.TradeItem
import com.belcobtm.presentation.screens.wallet.trade.mytrade.list.model.TradesLoadingItem
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MyTradesViewModel(
    private val observeMyTradesUseCase: ObserveMyTradesUseCase,
    private val deleteTradeUseCase: DeleteTradeUseCase
) : ViewModel() {

    private val _deleteTradeLoadingData = MutableLiveData<LoadingData<Unit>>()
    val deleteTradeLoadingData: LiveData<LoadingData<Unit>>
        get() = _deleteTradeLoadingData

    private val tradeToDelete = MutableLiveData<TradeItem?>()

    private val _tradesLiveData = MutableLiveData<Either<Failure, List<ListItem>>?>()
    val tradesLiveData: LiveData<Either<Failure, List<ListItem>>?>
        get() = _tradesLiveData

    init {
        tradeToDelete.observeForever {
            if (it != null) {
                deleteTrade(it.tradeId)
            }
        }
    }

    fun observeMyTrades() {
        viewModelScope.launch {
            observeMyTradesUseCase().collectLatest {
                _tradesLiveData.value = it
            }
        }
    }

    fun delete(item: TradeItem) {
        _tradesLiveData.value = Either.Right(listOf(TradesLoadingItem()))
        tradeToDelete.value = item
    }

    fun retryDelete() {
        deleteTrade(tradeToDelete.value?.tradeId ?: return)
    }

    private fun deleteTrade(tradeId: String) {
        deleteTradeUseCase(
            tradeId,
            onSuccess = { tradeToDelete.value = null },
            onError = { _deleteTradeLoadingData.value = LoadingData.Error(it) }
        )
    }
}