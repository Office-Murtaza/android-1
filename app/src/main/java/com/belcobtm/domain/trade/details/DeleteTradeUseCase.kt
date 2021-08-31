package com.belcobtm.domain.trade.details

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.trade.TradeRepository

class DeleteTradeUseCase(
    private val tradeRepository: TradeRepository
) : UseCase<Unit, String>() {

    override suspend fun run(params: String): Either<Failure, Unit> =
        tradeRepository.deleteTrade(params)
}