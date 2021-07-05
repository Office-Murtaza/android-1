package com.belcobtm.domain.trade.list

import com.belcobtm.data.websockets.order.OrdersObserver
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase

class StopObserveOrderDataUseCase(
    private val ordersObserver: OrdersObserver
) : UseCase<Unit, Unit>() {

    override suspend fun run(params: Unit): Either<Failure, Unit> {
        ordersObserver.disconnect()
        return Either.Right(Unit)
    }
}