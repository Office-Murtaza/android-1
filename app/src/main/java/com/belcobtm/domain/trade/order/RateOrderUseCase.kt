package com.belcobtm.domain.trade.order

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.trade.TradeRepository

class RateOrderUseCase(
    private val tradeRepository: TradeRepository
) : UseCase<Unit, RateOrderUseCase.Params>() {

    override suspend fun run(params: Params): Either<Failure, Unit> =
        tradeRepository.rateOrder(params.orderId, params.rate)

    data class Params(val orderId: String, val rate: Int)
}