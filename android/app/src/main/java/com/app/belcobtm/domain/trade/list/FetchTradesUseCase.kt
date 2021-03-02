package com.app.belcobtm.domain.trade.list

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.trade.TradeRepository

class FetchTradesUseCase(
    private val tradeRepository: TradeRepository
) : UseCase<Unit, Unit>() {
    override suspend fun run(params: Unit): Either<Failure, Unit> =
        Either.Right(tradeRepository.fetchTrades())
}