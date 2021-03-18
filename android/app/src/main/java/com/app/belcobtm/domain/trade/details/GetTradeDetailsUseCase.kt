package com.app.belcobtm.domain.trade.details

import com.app.belcobtm.data.model.trade.Trade
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.trade.TradeRepository
import com.app.belcobtm.domain.trade.list.mapper.TradeToTradeItemMapper
import com.app.belcobtm.presentation.features.wallet.trade.list.model.TradeItem

class GetTradeDetailsUseCase(
    private val tradeRepository: TradeRepository,
    private val tradeMapper: TradeToTradeItemMapper
) : UseCase<TradeItem, Int>() {

    override suspend fun run(params: Int): Either<Failure, TradeItem> {
        val result = tradeRepository.getTrade(params)
        return if (result.isLeft) {
            result as Either.Left<Failure>
        } else {
            Either.Right(tradeMapper.map((result as Either.Right<Trade>).b))
        }
    }
}