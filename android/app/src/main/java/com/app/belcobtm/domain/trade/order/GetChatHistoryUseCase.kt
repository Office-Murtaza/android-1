package com.app.belcobtm.domain.trade.order

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.trade.TradeRepository
import com.app.belcobtm.presentation.core.adapter.model.ListItem

class GetChatHistoryUseCase(
    private val tradeRepository: TradeRepository,
) : UseCase<List<ListItem>, Int>() {

    override suspend fun run(params: Int): Either<Failure, List<ListItem>> =
        Either.Right(emptyList())
}