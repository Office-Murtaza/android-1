package com.app.belcobtm.domain.trade.order

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.trade.TradeRepository

class CancelOrderUseCase(
    private val tradeRepository: TradeRepository
): UseCase<Unit, String>() {

    override suspend fun run(params: String): Either<Failure, Unit> =
        tradeRepository.cancelOrder(params)
}