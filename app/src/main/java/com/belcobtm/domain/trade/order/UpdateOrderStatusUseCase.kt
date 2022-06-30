package com.belcobtm.domain.trade.order

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.trade.TradeRepository
import com.belcobtm.presentation.screens.wallet.trade.order.details.model.UpdateOrderStatusItem

class UpdateOrderStatusUseCase(private val tradeRepository: TradeRepository) : UseCase<Unit, UpdateOrderStatusItem>() {

    override suspend fun run(params: UpdateOrderStatusItem): Either<Failure, Unit> =
        tradeRepository.updateOrder(params)
}