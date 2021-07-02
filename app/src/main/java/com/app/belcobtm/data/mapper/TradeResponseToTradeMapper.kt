package com.app.belcobtm.data.mapper

import com.app.belcobtm.data.inmemory.trade.TradeInMemoryCache
import com.app.belcobtm.data.model.trade.Trade
import com.app.belcobtm.data.rest.trade.response.TradeItemResponse

class TradeResponseToTradeMapper {

    private companion object {
        const val PAYMENT_DELIMITER = ","
    }

    fun map(trade: TradeItemResponse): Trade =
        with(trade) {
            Trade(
                id, type, coin, status, timestamp,
                price, minLimit, maxLimit, openOrders,
                paymentMethods.takeIf { it.isNotEmpty() }
                    ?.split(PAYMENT_DELIMITER)
                    ?.map(String::toInt)
                    .orEmpty(),
                terms, makerUserId, makerPublicId, makerStatus,
                makerLatitude, makerLongitude, makerTotalTrades,
                makerTradingRate, TradeInMemoryCache.UNDEFINED_DISTANCE
            )
        }
}