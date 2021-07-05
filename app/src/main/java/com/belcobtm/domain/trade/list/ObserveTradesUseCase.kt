package com.belcobtm.domain.trade.list

import com.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.belcobtm.data.model.trade.TradeData
import com.belcobtm.data.model.trade.TradeType
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.trade.TradeRepository
import com.belcobtm.domain.trade.list.mapper.TradesDataToTradeListMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn

class ObserveTradesUseCase(
    private val tradeRepository: TradeRepository,
    private val mapper: TradesDataToTradeListMapper,
    private val sharedPreferencesHelper: SharedPreferencesHelper
) {

    operator fun invoke(params: Params) =
        combine(tradeRepository.observeTradeData(), tradeRepository.observeFilter()) { tradeData, filter ->
            when {
                tradeData == null -> null
                tradeData.isRight ->
                    Either.Right(
                        mapper.map(
                            (tradeData as Either.Right<TradeData>).b,
                            params, filter,
                            sharedPreferencesHelper.userId
                        )
                    )
                else ->
                    tradeData as Either.Left<Failure>
            }
        }.flowOn(Dispatchers.Default)

    data class Params(val numbersToLoad: Int, @TradeType val tradeType: Int)
}