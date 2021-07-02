package com.app.belcobtm.domain.trade.order

import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.map
import com.app.belcobtm.domain.trade.TradeRepository
import com.app.belcobtm.domain.trade.list.mapper.TradeOrderDataToItemMapper
import com.app.belcobtm.presentation.features.wallet.trade.list.model.OrderItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class ObserveOrderDetailsUseCase(
    private val tradeRepository: TradeRepository,
    private val mapper: TradeOrderDataToItemMapper,
    private val sharedPreferencesHelper: SharedPreferencesHelper
) {

    operator fun invoke(params: String): Flow<Either<Failure, OrderItem?>> =
        tradeRepository.observeTradeData()
            .map {
                it?.map { tradeData ->
                    mapper.map(tradeData.orders[params], tradeData, sharedPreferencesHelper.userId)
                } ?: Either.Left(Failure.ServerError())
            }.flowOn(Dispatchers.Default)

}