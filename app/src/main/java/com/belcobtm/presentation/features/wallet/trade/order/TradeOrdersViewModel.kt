package com.belcobtm.presentation.features.wallet.trade.order

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.trade.list.ObserveOrdersUseCase
import com.belcobtm.presentation.features.wallet.trade.list.model.OrderItem

class TradeOrdersViewModel(
    private val observeOrdersUseCase: ObserveOrdersUseCase
) : ViewModel() {

    fun observeOrders(): LiveData<Either<Failure, List<OrderItem>>?> =
        observeOrdersUseCase()
            .asLiveData(viewModelScope.coroutineContext)

}