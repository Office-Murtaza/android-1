package com.belcobtm.domain.trade.details

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.trade.TradeRepository
import com.belcobtm.presentation.features.wallet.trade.edit.EditTradeItem

class EditTradeUseCase(
    private val tradeRepository: TradeRepository
) : UseCase<Unit, EditTradeItem>() {

    override suspend fun run(params: EditTradeItem): Either<Failure, Unit> =
        tradeRepository.editTrade(params)
}