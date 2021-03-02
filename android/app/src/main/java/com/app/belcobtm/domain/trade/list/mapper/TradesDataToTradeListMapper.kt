package com.app.belcobtm.domain.trade.list.mapper

import com.app.belcobtm.data.model.trade.Trade
import com.app.belcobtm.data.model.trade.TradeData
import com.app.belcobtm.domain.trade.list.ObserveTradesUseCase
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.presentation.features.wallet.trade.list.model.TradeItem

class TradesDataToTradeListMapper(
    private val paymentOptionMapper: TradePaymentOptionMapper
) {

    fun map(tradeData: TradeData, params: ObserveTradesUseCase.Params): List<TradeItem> =
        tradeData.trades
            .asSequence()
            .filter { it.type == params.tradeType }
            // TODO add another filter
            .take(params.numbersToLoad)
            .map(::mapTrade)
            // TODO distinct until changed?
            .toList()

    private fun mapTrade(trade: Trade): TradeItem =
        with(trade) {
            TradeItem(
                id, type, LocalCoinType.valueOf(coinCode),
                status, createDate, price, minLimit, maxLimit,
                paymentMethods.map(paymentOptionMapper::map),
                terms, makerId, makerPublicId, makerTotalTrades, makerTradingRate, distance
            )
        }
}