package com.belcobtm.domain.trade.order

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.map
import com.belcobtm.domain.trade.TradeRepository
import com.belcobtm.presentation.core.adapter.model.ListItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetChatHistoryUseCase(
    private val tradeRepository: TradeRepository,
) {

    operator fun invoke(orderId: String): Flow<Either<Failure, List<ListItem>>> =
        tradeRepository.observeTradeData()
            .map {
                it.map { tradeData ->
                    tradeData.orders.getValue(orderId).chatHistory
                }
            }

}
