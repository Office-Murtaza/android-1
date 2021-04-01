package com.app.belcobtm.domain.trade.details

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.trade.TradeRepository

class CancelTradeUseCase(
    private val tradeRepository: TradeRepository
) : UseCase<Unit, Int>() {

    override suspend fun run(params: Int): Either<Failure, Unit> =
        tradeRepository.cancelTrade(params)
}