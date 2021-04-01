package com.app.belcobtm.data.mapper

import com.app.belcobtm.data.model.trade.Order
import com.app.belcobtm.data.rest.trade.response.TradeOrderItemResponse

class OrderResponseToOrderMapper {

    fun map(trade: TradeOrderItemResponse): Order =
        with(trade) {
            Order(
                id, tradeId, coin, status, timestamp,
                price, cryptoAmount, fiatAmount, terms,
                makerId, makerStatus, makerPublicId, makerLatitude,
                makerLongitude, makerTotalTrades, makerTradingRate,
                takerId, takerStatus, takerPublicId, takerLatitude,
                takerLongitude, takerTotalTrades, takerTradingRate
            )
        }
}