package com.belcobtm.domain.trade.details

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.map
import com.belcobtm.domain.trade.TradeRepository
import com.belcobtm.domain.trade.list.mapper.TradeToTradeItemMapper
import com.belcobtm.presentation.screens.wallet.trade.list.model.TradeItem

class GetTradeDetailsUseCase(
    private val tradeRepository: TradeRepository,
    private val tradeMapper: TradeToTradeItemMapper
) : UseCase<TradeItem, String>() {

    override suspend fun run(params: String): Either<Failure, TradeItem> =
        tradeRepository.getTrade(params)
            .map(tradeMapper::map)
}