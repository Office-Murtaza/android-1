package com.app.belcobtm.domain.trade.list

import com.app.belcobtm.data.model.trade.TradeData
import com.app.belcobtm.data.model.trade.TradeType
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.trade.TradeRepository
import com.app.belcobtm.domain.trade.list.mapper.TradesDataToTradeListMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class ObserveTradesUseCase(
    private val tradeRepository: TradeRepository,
    private val mapper: TradesDataToTradeListMapper
) {

    operator fun invoke(params: Params) =
        tradeRepository.observeTradeData()
            .map {
                when {
                    it == null -> null
                    it.isRight ->
                        Either.Right(mapper.map((it as Either.Right<TradeData>).b, params))
                    else ->
                        it as Either.Left<Failure>
                }
            }.flowOn(Dispatchers.Default)

    data class Params(
        val numbersToLoad: Int,
        @TradeType val tradeType: Int
        // TODO add filters
    )
}