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
    val makerUsername: String,
    @TraderStatus val makerStatus: Int,
    val makerRate: Double?,
    val makerLocation: LocationResponse?,
    val makerTradeTotal: Int,
    val makerTradeRate: Double?,
    val takerUserId: String,
    @TraderStatus val takerStatus: Int,
    val takerRate: Double?,
    val takerUsername: String,
    val takerLocation: LocationResponse?,
    val takerTradeTotal: Int,
    val takerTradeRate: Double?
)