package com.app.belcobtm.data.mapper

import com.app.belcobtm.data.inmemory.TradeInMemoryCache.Companion.UNDEFINED_DISTANCE
import com.app.belcobtm.data.model.trade.Order
import com.app.belcobtm.data.model.trade.Trade
import com.app.belcobtm.data.model.trade.TradeData
import com.app.belcobtm.data.model.trade.UserTradeStatistics
import com.app.belcobtm.data.rest.trade.response.TradeItemResponse
import com.app.belcobtm.data.rest.trade.response.TradeOrderItemResponse
import com.app.belcobtm.data.rest.trade.response.TradesResponse

class TradesResponseToTradeDataMapper {

    private companion object {
        const val PAYMENT_DELIMITER = ","
    }

    fun map(response: TradesResponse): TradeData =
        with(response) {
            TradeData(
                trades.map(::mapTrade),
                orders.map(::mapOrder),
                mapStatistic(this)
            )
        }

    private fun mapTrade(trade: TradeItemResponse): Trade =
        with(trade) {
            Trade(
                id, type, coin,
                status, createDate,
                price, minLimit, maxLimit,
                paymentMethods.split(PAYMENT_DELIMITER).map(String::toInt),
                terms, makerId, makerPublicId, makerLatitude,
                makerLongitude, makerTotalTrades,
                makerTradingRate, UNDEFINED_DISTANCE
            )
        }

    private fun mapOrder(trade: TradeOrderItemResponse): Order =
        with(trade) {
            Order(
                id, tradeId, type, coin,
                status, createDate,
                price, paymentMethods.split(PAYMENT_DELIMITER).map(String::toInt),
                cryptoAmount, fiatAmount, terms,
                makerId, makerPublicId, makerLatitude,
                makerLongitude, makerTotalTrades, makerTradingRate,
                takerId, takerPublicId, takerLatitude,
                takerLongitude, takerTotalTrades, takerTradingRate
            )
        }

    private fun mapStatistic(response: TradesResponse): UserTradeStatistics =
        with(response) {
            UserTradeStatistics(publicId, status, totalTrades, tradingRate)
        }

}