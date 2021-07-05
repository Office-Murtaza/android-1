package com.belcobtm.domain.trade.create

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.trade.TradeRepository
import com.belcobtm.presentation.features.wallet.trade.create.model.CreateTradeItem

class CreateTradeUseCase(
    private val tradeRepository: TradeRepository
) : UseCase<Unit, CreateTradeItem>() {

    override suspend fun run(params: CreateTradeItem): Either<Failure, Unit> =
        tradeRepository.createTrade(params)
}