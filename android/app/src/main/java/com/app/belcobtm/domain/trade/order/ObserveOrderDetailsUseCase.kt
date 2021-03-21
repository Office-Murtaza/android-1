package com.app.belcobtm.domain.trade.order

import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.flatMap
import com.app.belcobtm.domain.map
import com.app.belcobtm.domain.trade.TradeRepository
import com.app.belcobtm.domain.trade.list.mapper.TradeOrderDataToItemMapper
import com.app.belcobtm.presentation.features.wallet.trade.list.model.OrderItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest

class ObserveOrderDetailsUseCase(
    private val tradeRepository: TradeRepository,
    private val mapper: TradeOrderDataToItemMapper,
    private val sharedPreferencesHelper: SharedPreferencesHelper
) {

    operator fun invoke(params: Int): Flow<Either<Failure, OrderItem>> =
        tradeRepository.observeTradeData()
            .mapLatest {
                val tradeDetails = tradeRepository.getOrderDetails(params)
                it?.flatMap { tradeData ->
                    tradeDetails.map { order ->
                        mapper.map(order, tradeData, sharedPreferencesHelper.userId)
                    }
                } ?: Either.Left(Failure.ServerError())
            }

}