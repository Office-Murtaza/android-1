package com.app.belcobtm.data.rest.trade.response

import com.app.belcobtm.data.model.trade.OrderStatus
import com.app.belcobtm.data.model.trade.TraderStatus

data class TradeOrderItemResponse(
    val id: String,
    val tradeId: String,
    val coin: String,
    @OrderStatus val status: Int,
    val timestamp: Long,
    val price: Double,
    val cryptoAmount: Double,
    val fiatAmount: Double,
    val terms: String,
    val makerId: Int,
    val makerPublicId: String,
    @TraderStatus val makerStatus: Int,
    val makerLatitude: Double?,
    val makerLongitude: Double?,
    val makerTotalTrades: Int,
    val makerTradingRate: Double?,
    val takerId: Int,
    @TraderStatus val takerStatus: Int,
    val takerPublicId: String,
    val takerLatitude: Double?,
    val takerLongitude: Double?,
    val takerTotalTrades: Int,
    val takerTradingRate: Double?
)