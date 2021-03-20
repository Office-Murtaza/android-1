package com.app.belcobtm.data.model.trade

data class Order(
    val id: Int,
    val tradeId: Int,
    @TradeType val type: Int,
    val coinCode: String,
    @OrderStatus val status: Int,
    val timestamp: Long,
    val price: Double,
    val paymentMethods: List<@PaymentOption Int>,
    val cryptoAmount: Double,
    val fiatAmount: Double,
    val terms: String,
    val makerId: Int,
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