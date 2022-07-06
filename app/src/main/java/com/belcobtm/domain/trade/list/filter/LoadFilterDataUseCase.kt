package com.belcobtm.domain.trade.list.filter

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.trade.TradeRepository
import com.belcobtm.presentation.screens.wallet.trade.list.filter.model.TradeFilterItem

class LoadFilterDataUseCase(private val tradeRepository: TradeRepository) : UseCase<TradeFilterItem, Unit>() {

    override suspend fun run(params: Unit): Either<Failure, TradeFilterItem> {
        return Either.Right(tradeRepository.getFilterItem())
    }
}