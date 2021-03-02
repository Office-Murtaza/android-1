package com.app.belcobtm.domain.trade.list

import com.app.belcobtm.data.model.trade.TradeData
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.trade.TradeRepository
import com.app.belcobtm.domain.trade.list.mapper.TradesDataToStatisticsMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class ObserveUserTradeStatisticUseCase(
    private val tradeRepository: TradeRepository,
    private val mapper: TradesDataToStatisticsMapper
) {

    operator fun invoke() =
        tradeRepository.observeTradeData()
            .map {
                when {
                    it == null -> null
                    it.isRight ->
                        Either.Right(mapper.map((it as Either.Right<TradeData>).b))
                    else ->
                        it as Either.Left<Failure>
                }
            }.flowOn(Dispatchers.Default)

}