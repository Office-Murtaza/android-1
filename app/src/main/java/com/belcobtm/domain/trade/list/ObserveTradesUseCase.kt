package com.belcobtm.domain.trade.list

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.PreferencesInteractor
import com.belcobtm.domain.trade.TradeRepository
import com.belcobtm.domain.trade.list.mapper.TradesDataToTradeListMapper
import com.belcobtm.domain.trade.model.TradeHistoryDomainModel
import com.belcobtm.domain.trade.model.trade.TradeType
import kotlinx.coroutines.flow.combine

class ObserveTradesUseCase(
    private val tradeRepository: TradeRepository,
    private val mapper: TradesDataToTradeListMapper,
    private val preferences: PreferencesInteractor
) {

    operator fun invoke(params: Params) =
        combine(tradeRepository.observeTradeData(), tradeRepository.observeFilter()) { tradeData, filter ->
            when {
                tradeData.isRight ->
                    Either.Right(
                        mapper.map(
                            tradeData = (tradeData as Either.Right<TradeHistoryDomainModel>).b,
                            params = params,
                            filter = filter,
                            userId = preferences.userId
                        )
                    )
                else ->
                    tradeData as Either.Left<Failure>
            }
        }

    data class Params(val numbersToLoad: Int, val tradeType: TradeType)

}
