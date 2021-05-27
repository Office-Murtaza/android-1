package com.app.belcobtm.domain.trade.list.filter

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.trade.TradeRepository
import com.app.belcobtm.presentation.features.wallet.trade.list.filter.model.TradeFilterItem

class LoadFilterDataUseCase(private val tradeRepository: TradeRepository) : UseCase<TradeFilterItem, Unit>() {

    override suspend fun run(params: Unit): Either<Failure, TradeFilterItem> {
        return Either.Right(tradeRepository.getFilterItem())
    }
}