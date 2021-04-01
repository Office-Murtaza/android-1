package com.app.belcobtm.domain.trade.order

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.trade.TradeRepository
import com.app.belcobtm.presentation.features.wallet.trade.order.create.model.TradeOrderItem

class CreateOrderUseCase(private val tradeRepository: TradeRepository) : UseCase<Int, TradeOrderItem>() {

    override suspend fun run(params: TradeOrderItem): Either<Failure, Int> =
        tradeRepository.createOrder(params)
}