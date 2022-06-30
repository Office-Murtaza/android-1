package com.belcobtm.domain.trade.create

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.trade.TradeRepository
import com.belcobtm.domain.trade.create.mapper.PaymentIdToAvailablePaymentOptionMapper
import com.belcobtm.presentation.screens.wallet.trade.create.model.AvailableTradePaymentOption

class GetAvailableTradePaymentOptionsUseCase(
    private val repository: TradeRepository,
    private val mapper: PaymentIdToAvailablePaymentOptionMapper
) : UseCase<List<AvailableTradePaymentOption>, Unit>() {

    override suspend fun run(params: Unit): Either<Failure, List<AvailableTradePaymentOption>> =
        Either.Right(repository.getAvailablePaymentOptions().map(mapper::map))
}