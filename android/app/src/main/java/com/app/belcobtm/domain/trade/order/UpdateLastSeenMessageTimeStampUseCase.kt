package com.app.belcobtm.domain.trade.order

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.trade.TradeRepository

class UpdateLastSeenMessageTimeStampUseCase(
    private val tradeRepository: TradeRepository
) : UseCase<Unit, Unit>() {

    override suspend fun run(params: Unit): Either<Failure, Unit> =
        Either.Right(tradeRepository.updateLastSeenMessageTimestamp())
}