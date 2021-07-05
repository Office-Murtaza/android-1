package com.belcobtm.domain.trade.list

import com.belcobtm.data.websockets.trade.TradesObserver
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase

class StopObserveTradeDataUseCase(
    private val tradesObserver: TradesObserver
) : UseCase<Unit, Unit>() {

    override suspend fun run(params: Unit): Either<Failure, Unit> {
        tradesObserver.disconnect()
        return Either.Right(Unit)
    }
}