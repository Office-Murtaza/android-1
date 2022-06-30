package com.belcobtm.domain.trade.list.filter

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.trade.TradeRepository
import com.belcobtm.domain.trade.list.filter.mapper.TradeFilterMapper
import com.belcobtm.presentation.screens.wallet.trade.list.filter.model.TradeFilterItem

class ApplyFilterUseCase(
    private val tradeRepository: TradeRepository,
    private val mapper: TradeFilterMapper
) : UseCase<Unit, TradeFilterItem>() {

    override suspend fun run(params: TradeFilterItem): Either<Failure, Unit> {
        tradeRepository.updateFilter(mapper.map(params))
        return Either.Right(Unit)
    }
}