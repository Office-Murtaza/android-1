package com.belcobtm.data.model.trade

data class Trade(
    val id: String,
    @TradeType val type: Int,
    val coinCode: String,
    @TradeStatus val status: Int,
    val timestamp: Long,
    val price: Double,
    val minLimit: Double,
    val maxLimit: Double,
    val ordersCount: Int,
    val paymentMethods: List<@PaymentOption Int>,
    val terms: String,
    val makerId: String,
    val makerUsername: String,
    @TraderStatus val makerStatus: Int,
    val makerLatitude: Double?,
    val makerLongitude: Double?,
    val makerTotalTrades: Int,
    val makerTradingRate: Double,
    var distance: Double
)