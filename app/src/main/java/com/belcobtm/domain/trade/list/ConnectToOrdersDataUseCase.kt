package com.belcobtm.domain.trade.list

import com.belcobtm.data.websockets.order.OrdersObserver
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase

class ConnectToOrdersDataUseCase(
    private val ordersObserver: OrdersObserver
) : UseCase<Unit, Unit>() {

    override suspend fun run(params: Unit): Either<Failure, Unit> {
        ordersObserver.connect()
        return Either.Right(Unit)
    }
}