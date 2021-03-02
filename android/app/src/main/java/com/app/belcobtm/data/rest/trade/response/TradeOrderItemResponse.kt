package com.app.belcobtm.data.rest.trade.response

import com.app.belcobtm.data.model.trade.OrderStatus
import com.app.belcobtm.data.model.trade.TradeType

data class TradeOrderItemResponse(
    val id: Int,
    val tradeId: Int,
    @TradeType val type: Int,
    val coin: String,
    @OrderStatus val status: Int,
    val createDate: String,
    val price: Double,
    val cryptoAmount: Double,
    val fiatAmount: Double,
    val terms: String,
    val makerId: Int,
    val paymentMethods: String,
    val makerPublicId: String,
    val makerLatitude: Double?,
    val makerLongitude: Double?,
    val makerTotalTrades: Int,
    val makerTradingRate: Double,
    val takerId: Int,
    val takerPublicId: String,
    val takerLatitude: Double?,
    val takerLongitude: Double?,
    val takerTotalTrades: Int,
    val takerTradingRate: Double
)