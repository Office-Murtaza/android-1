package com.app.belcobtm.domain.trade.create

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.trade.TradeRepository
import com.app.belcobtm.domain.trade.create.mapper.PaymentIdToAvailablePaymentOptionMapper
import com.app.belcobtm.presentation.features.wallet.trade.create.model.AvailableTradePaymentOption

class GetAvailableTradePaymentOptionsUseCase(
    private val repository: TradeRepository,
    private val mapper: PaymentIdToAvailablePaymentOptionMapper
) : UseCase<List<AvailableTradePaymentOption>, Unit>() {

    override suspend fun run(params: Unit): Either<Failure, List<AvailableTradePaymentOption>> =
        Either.Right(repository.getAvailablePaymentOptions().map(mapper::map))
}