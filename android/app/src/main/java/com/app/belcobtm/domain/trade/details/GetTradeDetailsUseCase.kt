package com.app.belcobtm.domain.trade.details

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.map
import com.app.belcobtm.domain.trade.TradeRepository
import com.app.belcobtm.domain.trade.list.mapper.TradeToTradeItemMapper
import com.app.belcobtm.presentation.features.wallet.trade.list.model.TradeItem

class GetTradeDetailsUseCase(
    private val tradeRepository: TradeRepository,
    private val tradeMapper: TradeToTradeItemMapper
) : UseCase<TradeItem, Int>() {

    override suspend fun run(params: Int): Either<Failure, TradeItem> =
        tradeRepository.getTrade(params)
            .map(tradeMapper::map)
}