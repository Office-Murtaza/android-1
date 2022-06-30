package com.belcobtm.presentation.screens.wallet.trade.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.belcobtm.data.model.trade.TradeType
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.trade.list.ObserveTradesUseCase
import com.belcobtm.domain.trade.list.filter.ResetFilterUseCase
import com.belcobtm.presentation.screens.wallet.trade.list.model.TradeItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest

@ExperimentalCoroutinesApi
class TradeListViewModel(
    private val observeTradesUseCase: ObserveTradesUseCase,
    private val resetFilterUseCase: ResetFilterUseCase
) : ViewModel() {

    companion object {
        private const val PAGE_SIZE = 10
    }

    val lastVisibleItem = MutableStateFlow(0)

    fun observeTrades(@TradeType tradeType: Int): LiveData<Either<Failure, List<TradeItem>>?> =
        lastVisibleItem.flatMapLatest { lastItem ->
            val numbersToLoad = lastItem + PAGE_SIZE
            observeTradesUseCase.invoke(ObserveTradesUseCase.Params(numbersToLoad, tradeType))
        }.asLiveData(viewModelScope.coroutineContext)

    fun loadNextPage(itemsCount: Int) {
        lastVisibleItem.value = itemsCount
    }

    fun resetFilters() {
        resetFilterUseCase.invoke(Unit)
    }
}