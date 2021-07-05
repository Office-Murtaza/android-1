package com.belcobtm.domain.trade

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase

class ClearCacheUseCase(
    private val tradeRepository: TradeRepository
) : UseCase<Unit, Unit>() {

    override suspend fun run(params: Unit): Either<Failure, Unit> {
        tradeRepository.clearCache()
        return Either.Right(Unit)
    }
}