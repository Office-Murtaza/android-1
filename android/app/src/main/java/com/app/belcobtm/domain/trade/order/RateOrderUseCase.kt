package com.app.belcobtm.domain.trade.order

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.trade.TradeRepository

class RateOrderUseCase(
    private val tradeRepository: TradeRepository
) : UseCase<Unit, RateOrderUseCase.Params>() {

    override suspend fun run(params: Params): Either<Failure, Unit> =
        tradeRepository.rateOrder(params.orderId, params.rate)

    data class Params(val orderId: Int, val rate: Int)
}