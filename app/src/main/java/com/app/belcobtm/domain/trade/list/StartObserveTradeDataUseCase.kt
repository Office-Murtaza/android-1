package com.app.belcobtm.domain.trade.list

import com.app.belcobtm.data.websockets.trade.TradesObserver
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase

class StartObserveTradeDataUseCase(
    private val tradesObserver: TradesObserver
) : UseCase<Unit, Unit>() {

    override suspend fun run(params: Unit): Either<Failure, Unit> {
        tradesObserver.connect()
        return Either.Right(Unit)
    }
}