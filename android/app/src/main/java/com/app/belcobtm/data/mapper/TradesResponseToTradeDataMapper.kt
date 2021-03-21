package com.app.belcobtm.data.mapper

import com.app.belcobtm.data.model.trade.TradeData
import com.app.belcobtm.data.model.trade.UserTradeStatistics
import com.app.belcobtm.data.rest.trade.response.TradesResponse

class TradesResponseToTradeDataMapper(
    private val orderMapper: OrderResponseToOrderMapper,
    private val tradeMapper: TradeResponseToTradeMapper
) {

    fun map(response: TradesResponse): TradeData =
        with(response) {
            TradeData(
                trades.map(tradeMapper::map),
                orders.map(orderMapper::map),
                mapStatistic(this)
            )
        }

    private fun mapStatistic(response: TradesResponse): UserTradeStatistics =
        with(response) {
            UserTradeStatistics(publicId, status, totalTrades, tradingRate)
        }

}