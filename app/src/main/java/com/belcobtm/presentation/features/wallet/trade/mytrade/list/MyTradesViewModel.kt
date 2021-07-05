package com.belcobtm.presentation.features.wallet.trade.mytrade.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.trade.list.ObserveMyTradesUseCase
import com.belcobtm.presentation.core.adapter.model.ListItem

class MyTradesViewModel(
    private val observeMyTradesUseCase: ObserveMyTradesUseCase
) : ViewModel() {

    fun observeMyTrades(): LiveData<Either<Failure, List<ListItem>>?> =
        observeMyTradesUseCase()
            .asLiveData(viewModelScope.coroutineContext)
}