package com.belcobtm.presentation.features.wallet.trade.statistic

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.trade.list.ObserveUserTradeStatisticUseCase
import com.belcobtm.presentation.features.wallet.trade.list.model.TradeStatistics

class TradeUserStatisticViewModel(
    private val observeUserTradeStatisticUseCase: ObserveUserTradeStatisticUseCase
) : ViewModel() {

    fun observeStatistic(): LiveData<Either<Failure, TradeStatistics>?> =
        observeUserTradeStatisticUseCase.invoke()
            .asLiveData(viewModelScope.coroutineContext)

}