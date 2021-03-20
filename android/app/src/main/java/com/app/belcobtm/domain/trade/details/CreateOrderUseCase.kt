package com.app.belcobtm.domain.trade.details

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.trade.TradeRepository
import com.app.belcobtm.presentation.features.wallet.trade.buysell.model.TradeOrderItem

class CreateOrderUseCase(private val tradeRepository: TradeRepository) : UseCase<Unit, TradeOrderItem>() {

    override suspend fun run(params: TradeOrderItem): Either<Failure, Unit> =
        tradeRepository.createOrder(params)
}