package com.app.belcobtm.domain.trade

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase

class ClearCacheUseCase(
    private val tradeRepository: TradeRepository
) : UseCase<Unit, Unit>() {

    override suspend fun run(params: Unit): Either<Failure, Unit> {
        tradeRepository.clearCache()
        return Either.Right(Unit)
    }
}