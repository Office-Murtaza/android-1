package com.app.belcobtm.domain.trade.details

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.trade.TradeRepository
import com.app.belcobtm.presentation.features.wallet.trade.edit.EditTradeItem

class EditTradeUseCase(
    private val tradeRepository: TradeRepository
) : UseCase<Unit, EditTradeItem>() {

    override suspend fun run(params: EditTradeItem): Either<Failure, Unit> =
        tradeRepository.editTrade(params)
}