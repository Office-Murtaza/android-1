package com.belcobtm.domain.trade.order

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.PreferencesInteractor
import com.belcobtm.domain.map
import com.belcobtm.domain.trade.TradeRepository
import com.belcobtm.domain.trade.list.mapper.TradeOrderDataToItemMapper
import com.belcobtm.presentation.screens.wallet.trade.list.model.OrderItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class ObserveOrderDetailsUseCase(
    private val tradeRepository: TradeRepository,
    private val mapper: TradeOrderDataToItemMapper,
    private val preferences: PreferencesInteractor
) {

    operator fun invoke(params: String): Flow<Either<Failure, OrderItem?>> =
        tradeRepository.observeTradeData()
            .map {
                it?.map { tradeData ->
                    mapper.map(tradeData.orders[params], tradeData, preferences.userId)
                } ?: Either.Left(Failure.ServerError())
            }.flowOn(Dispatchers.Default)

}
