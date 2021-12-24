package com.belcobtm.data.mapper

import com.belcobtm.data.model.trade.Order
import com.belcobtm.data.rest.trade.response.TradeOrderItemResponse
import com.belcobtm.presentation.features.wallet.trade.order.chat.model.ChatMessageItem

class OrderResponseToOrderMapper {

    fun map(
        trade: TradeOrderItemResponse,
        chatHistory: List<ChatMessageItem>
    ): Order =
        with(trade) {
            Order(
                id, tradeId, coin, status, timestamp,
                price, cryptoAmount, fiatAmount, terms,
                makerUserId, makerStatus, makerRate, makerUsername,
                makerLocation?.latitude, makerLocation?.longitude, makerTradeTotal,
                makerTradeRate, takerUserId, takerStatus, takerRate,
                takerUsername, takerLocation?.latitude, takerLocation?.longitude,
                takerTradeTotal, takerTradeRate, chatHistory
            )
        }
}