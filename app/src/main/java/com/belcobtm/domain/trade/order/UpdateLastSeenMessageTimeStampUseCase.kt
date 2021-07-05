package com.belcobtm.domain.trade.order

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.trade.TradeRepository

class UpdateLastSeenMessageTimeStampUseCase(
    private val tradeRepository: TradeRepository
) : UseCase<Unit, Unit>() {

    override suspend fun run(params: Unit): Either<Failure, Unit> =
        Either.Right(tradeRepository.updateLastSeenMessageTimestamp())
}