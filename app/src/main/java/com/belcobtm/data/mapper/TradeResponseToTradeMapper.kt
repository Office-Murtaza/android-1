package com.belcobtm.data.mapper

import com.belcobtm.data.inmemory.trade.TradeInMemoryCache
import com.belcobtm.data.model.trade.Trade
import com.belcobtm.data.rest.trade.response.TradeItemResponse

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
                terms, makerUserId, makerUsername.orEmpty(), makerStatus,
                makerLatitude, makerLongitude, makerTradeTotal,
                makerTradeRate, TradeInMemoryCache.UNDEFINED_DISTANCE
            )
        }
}