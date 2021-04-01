package com.app.belcobtm.domain.trade.list.filter

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.trade.TradeRepository

class ResetFilterUseCase(
    private val tradeRepository: TradeRepository
) : UseCase<Unit, Unit>() {

    override suspend fun run(params: Unit): Either<Failure, Unit> {
        tradeRepository.resetFilters()
        return Either.Right(Unit)
    }
}