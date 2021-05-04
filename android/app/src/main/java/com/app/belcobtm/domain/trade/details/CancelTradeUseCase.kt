package com.app.belcobtm.domain.trade.details

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.trade.TradeRepository

class CancelTradeUseCase(
    private val tradeRepository: TradeRepository
) : UseCase<Unit, String>() {

    override suspend fun run(params: String): Either<Failure, Unit> =
        tradeRepository.cancelTrade(params)
}