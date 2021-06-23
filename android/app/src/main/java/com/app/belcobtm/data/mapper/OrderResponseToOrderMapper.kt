package com.app.belcobtm.data.mapper

import com.app.belcobtm.data.model.trade.Order
import com.app.belcobtm.data.rest.trade.response.TradeOrderItemResponse
import com.app.belcobtm.presentation.features.wallet.trade.order.chat.model.ChatMessageItem

class OrderResponseToOrderMapper() {

    fun map(
        trade: TradeOrderItemResponse,
        chatHistory: List<ChatMessageItem>
    ): Order =
        with(trade) {
            Order(
                id, tradeId, coin, status, timestamp,
                price, cryptoAmount, fiatAmount, terms,
                makerUserId, makerStatus, makerRate, makerPublicId,
                makerLatitude, makerLongitude, makerTotalTrades,
                makerTradingRate, takerUserId, takerStatus, takerRate,
                takerPublicId, takerLatitude, takerLongitude,
                takerTotalTrades, takerTradingRate, chatHistory
            )
        }
}