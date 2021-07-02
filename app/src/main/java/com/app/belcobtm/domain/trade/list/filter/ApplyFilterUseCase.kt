package com.app.belcobtm.domain.trade.list.filter

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.trade.TradeRepository
import com.app.belcobtm.domain.trade.list.filter.mapper.TradeFilterMapper
import com.app.belcobtm.presentation.features.wallet.trade.list.filter.model.TradeFilterItem

class ApplyFilterUseCase(
    private val tradeRepository: TradeRepository,
    private val mapper: TradeFilterMapper
) : UseCase<Unit, TradeFilterItem>() {

    override suspend fun run(params: TradeFilterItem): Either<Failure, Unit> {
        tradeRepository.updateFilter(mapper.map(params))
        return Either.Right(Unit)
    }
}