package com.belcobtm.domain.trade.details

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.trade.TradeRepository
import com.belcobtm.domain.trade.list.mapper.TradeToTradeItemMapper
import com.belcobtm.domain.trade.model.TradeHistoryDomainModel
import com.belcobtm.presentation.screens.wallet.trade.list.model.TradeItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveTradeDetailsUseCase(
    private val repository: TradeRepository,
    private val mapper: TradeToTradeItemMapper
) {

    operator fun invoke(params: String): Flow<Either<Failure, TradeItem>> =
        repository.observeTradeData()
            .map { tradeData ->
                tradeData.takeIf { it.isRight }?.let { data ->
                    (data as Either.Right<TradeHistoryDomainModel>).b
                        .trades[params]
                        ?.let { trade ->
                            Either.Right(mapper.map(trade))
                        }
                } ?: tradeData as Either.Left<Failure>
            }

}
