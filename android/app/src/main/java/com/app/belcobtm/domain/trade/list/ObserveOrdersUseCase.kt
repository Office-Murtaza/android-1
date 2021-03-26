package com.app.belcobtm.domain.trade.list

import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.data.model.trade.TradeData
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.trade.TradeRepository
import com.app.belcobtm.domain.trade.list.mapper.TradesDataToOrderListMapper
import com.app.belcobtm.presentation.features.wallet.trade.list.model.OrderItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class ObserveOrdersUseCase(
    private val tradeRepository: TradeRepository,
    private val mapper: TradesDataToOrderListMapper,
    private val preferencesHelper: SharedPreferencesHelper
) {

    operator fun invoke(): Flow<Either<Failure, List<OrderItem>>?> =
        tradeRepository.observeTradeData()
            .map {
                when {
                    it == null -> null
                    it.isRight ->
                        Either.Right(mapper.map((it as Either.Right<TradeData>).b, preferencesHelper.userId))
                    else ->
                        it as Either.Left<Failure>
                }
            }.flowOn(Dispatchers.Default)

}