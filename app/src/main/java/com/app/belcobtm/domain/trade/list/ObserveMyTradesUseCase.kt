package com.app.belcobtm.domain.trade.list

import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.data.model.trade.TradeData
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.trade.TradeRepository
import com.app.belcobtm.domain.trade.list.mapper.TradesDataToMyTradeMapper
import com.app.belcobtm.presentation.core.adapter.model.ListItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class ObserveMyTradesUseCase(
    private val tradeRepository: TradeRepository,
    private val sharedPreferencesHelper: SharedPreferencesHelper,
    private val mapper: TradesDataToMyTradeMapper
) {

    operator fun invoke(): Flow<Either<Failure, List<ListItem>>?> =
        tradeRepository.observeTradeData()
            .map {
                when {
                    it == null -> null
                    it.isRight ->
                        Either.Right(mapper.map((it as Either.Right<TradeData>).b, sharedPreferencesHelper.userId))
                    else ->
                        it as Either.Left<Failure>
                }
            }.flowOn(Dispatchers.Default)
}