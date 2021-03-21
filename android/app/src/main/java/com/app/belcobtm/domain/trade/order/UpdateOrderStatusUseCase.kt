package com.app.belcobtm.domain.trade.order

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.trade.TradeRepository
import com.app.belcobtm.presentation.features.wallet.trade.order.details.model.UpdateOrderStatusItem

class UpdateOrderStatusUseCase(private val tradeRepository: TradeRepository) : UseCase<Unit, UpdateOrderStatusItem>() {

    override suspend fun run(params: UpdateOrderStatusItem): Either<Failure, Unit> =
        tradeRepository.updateOrder(params)
}