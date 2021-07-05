package com.belcobtm.domain.trade.order

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.trade.TradeRepository
import com.belcobtm.presentation.features.wallet.trade.order.create.model.TradeOrderItem

class CreateOrderUseCase(private val tradeRepository: TradeRepository) : UseCase<String, TradeOrderItem>() {

    override suspend fun run(params: TradeOrderItem): Either<Failure, String> =
        tradeRepository.createOrder(params)
}