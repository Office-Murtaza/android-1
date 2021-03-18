package com.app.belcobtm.domain.trade.list

import com.app.belcobtm.data.model.trade.TradeData
import com.app.belcobtm.data.model.trade.TradeType
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.trade.TradeRepository
import com.app.belcobtm.domain.trade.list.mapper.TradesDataToTradeListMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn

class ObserveTradesUseCase(
    private val tradeRepository: TradeRepository,
    private val mapper: TradesDataToTradeListMapper
) {

    operator fun invoke(params: Params) =
        combine(tradeRepository.observeTradeData(), tradeRepository.observeFilter()) { tradeData, filter ->
            when {
                tradeData == null -> null
                tradeData.isRight ->
                    Either.Right(mapper.map((tradeData as Either.Right<TradeData>).b, params, filter))
                else ->
                    tradeData as Either.Left<Failure>
            }
        }.flowOn(Dispatchers.Default)

    data class Params(val numbersToLoad: Int, @TradeType val tradeType: Int)
}