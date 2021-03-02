package com.app.belcobtm.presentation.features.wallet.trade.order

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.trade.list.ObserveOrdersUseCase
import com.app.belcobtm.presentation.features.wallet.trade.list.model.OrderItem

class TradeOrdersViewModel(
    private val observeOrdersUseCase: ObserveOrdersUseCase
) : ViewModel() {

    fun observeOrders(): LiveData<Either<Failure, List<OrderItem>>?> =
        observeOrdersUseCase(Unit)
            .asLiveData(viewModelScope.coroutineContext)

}