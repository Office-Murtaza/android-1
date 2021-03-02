package com.app.belcobtm.presentation.features.wallet.trade.statistic

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.trade.list.ObserveUserTradeStatisticUseCase
import com.app.belcobtm.presentation.features.wallet.trade.list.model.TradeStatistics

class TradeUserStatisticViewModel(
    private val observeUserTradeStatisticUseCase: ObserveUserTradeStatisticUseCase
) : ViewModel() {

    fun observeStatistic(): LiveData<Either<Failure, TradeStatistics>?> =
        observeUserTradeStatisticUseCase.invoke()
            .asLiveData(viewModelScope.coroutineContext)

}