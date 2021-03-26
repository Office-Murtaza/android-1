package com.app.belcobtm.domain.trade.create

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.trade.TradeRepository
import com.app.belcobtm.presentation.features.wallet.trade.create.model.CreateTradeItem

class CreateTradeUseCase(
    private val tradeRepository: TradeRepository
) : UseCase<Unit, CreateTradeItem>() {

    override suspend fun run(params: CreateTradeItem): Either<Failure, Unit> =
        tradeRepository.createTrade(params)
}