package com.belcobtm.data.rest.trade.response

import com.belcobtm.data.model.trade.OrderStatus
import com.belcobtm.data.model.trade.TraderStatus

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
    val makerUserId: String,
    val makerPublicId: String,
    @TraderStatus val makerStatus: Int,
    val makerRate: Double?,
    val makerLatitude: Double?,
    val makerLongitude: Double?,
    val makerTotalTrades: Int,
    val makerTradingRate: Double?,
    val takerUserId: String,
    @TraderStatus val takerStatus: Int,
    val takerRate: Double?,
    val takerPublicId: String,
    val takerLatitude: Double?,
    val takerLongitude: Double?,
    val takerTotalTrades: Int,
    val takerTradingRate: Double?
)