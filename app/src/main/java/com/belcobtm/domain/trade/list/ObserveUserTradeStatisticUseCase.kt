package com.belcobtm.domain.trade.list

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.trade.TradeRepository
import com.belcobtm.domain.trade.list.mapper.TradesDataToStatisticsMapper
import com.belcobtm.domain.trade.model.TradeHistoryDomainModel
import kotlinx.coroutines.flow.map

class ObserveUserTradeStatisticUseCase(
    private val tradeRepository: TradeRepository,
    private val mapper: TradesDataToStatisticsMapper
) {

    operator fun invoke() =
        tradeRepository.observeTradeData()
            .map {
                when {
                    it.isRight ->
                        Either.Right(mapper.map((it as Either.Right<TradeHistoryDomainModel>).b))
                    else ->
                        it as Either.Left<Failure>
                }
            }

}
