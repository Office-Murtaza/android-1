package com.app.belcobtm.domain.trade.list.mapper

import com.app.belcobtm.data.model.trade.Trade
import com.app.belcobtm.data.model.trade.TradeData
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.presentation.core.adapter.model.ListItem
import com.app.belcobtm.presentation.features.wallet.trade.list.model.TradeItem
import com.app.belcobtm.presentation.features.wallet.trade.mytrade.list.model.NoTradesCreatedItem

class TradesDataToMyTradeMapper(
    private val paymentOptionMapper: TradePaymentOptionMapper
) {

    fun map(tradeData: TradeData, userId: Int): List<ListItem> =
        tradeData.trades
            .asSequence()
            .filter { it.makerId == userId }
            .map(::mapTrade)
            .toList()
            .ifEmpty { listOf(NoTradesCreatedItem()) }

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