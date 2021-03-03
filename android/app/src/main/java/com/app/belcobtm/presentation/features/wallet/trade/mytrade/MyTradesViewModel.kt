package com.app.belcobtm.presentation.features.wallet.trade.mytrade

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.trade.list.ObserveMyTradesUseCase
import com.app.belcobtm.presentation.core.adapter.model.ListItem

class MyTradesViewModel(
    private val observeMyTradesUseCase: ObserveMyTradesUseCase
) : ViewModel() {

    fun observeMyTrades(): LiveData<Either<Failure, List<ListItem>>?> =
        observeMyTradesUseCase()
            .asLiveData(viewModelScope.coroutineContext)
}