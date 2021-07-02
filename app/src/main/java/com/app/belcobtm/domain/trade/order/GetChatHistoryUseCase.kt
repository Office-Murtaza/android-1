package com.app.belcobtm.domain.trade.order

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.map
import com.app.belcobtm.domain.trade.TradeRepository
import com.app.belcobtm.presentation.core.adapter.model.ListItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetChatHistoryUseCase(
    private val tradeRepository: TradeRepository,
) {

    operator fun invoke(orderId: String): Flow<Either<Failure, List<ListItem>>> =
        tradeRepository.observeTradeData()
            .map {
                it?.map { tradeData ->
                    tradeData.orders.getValue(orderId).chatHistory
                } ?: Either.Left(Failure.ServerError())
            }
}