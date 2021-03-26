package com.app.belcobtm.domain.trade.list

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.trade.TradeRepository

class FetchTradesUseCase(
    private val tradeRepository: TradeRepository
) : UseCase<Unit, FetchTradesUseCase.Params>() {

    override suspend fun run(params: Params): Either<Failure, Unit> =
        Either.Right(tradeRepository.fetchTrades(params.calculateDistanceEnabled))

    data class Params(val calculateDistanceEnabled: Boolean)
}