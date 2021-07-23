package com.belcobtm.domain.trade.details

import com.belcobtm.data.model.trade.TradeData
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.trade.TradeRepository
import com.belcobtm.domain.trade.list.mapper.TradeToTradeItemMapper
import com.belcobtm.presentation.features.wallet.trade.list.model.TradeItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class ObserveTradeDetailsUseCase(
    private val repository: TradeRepository,
    private val mapper: TradeToTradeItemMapper
) {

    operator fun invoke(params: String): Flow<Either<Failure, TradeItem>?> =
        repository.observeTradeData()
            .map { tradeData ->
                when {
                    tradeData == null -> null
                    tradeData.isRight -> {
                        val trade = (tradeData as Either.Right<TradeData>).b.trades[params]
                        trade?.let {
                            Either.Right(mapper.map(it))
                        }
                    }
                    else ->
                        tradeData as Either.Left<Failure>
                }
            }.flowOn(Dispatchers.Default)
}