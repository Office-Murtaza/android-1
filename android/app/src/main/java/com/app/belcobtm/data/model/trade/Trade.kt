package com.app.belcobtm.data.model.trade

data class Trade(
    val id: Int,
    @TradeType val type: Int,
    val coinCode: String,
    @TradeStatus val status: Int,
    val createDate: String,
    val price: Double,
    val minLimit: Double,
    val maxLimit: Double,
    val paymentMethods: List<@PaymentOption Int>,
    val terms: String,
    val makerId: Int,
    val makerPublicId: String,
    val makerLatitude: Double?,
    val makerLongitude: Double?,
    val makerTotalTrades: Int,
    val makerTradingRate: Double,
    val distance: Double
)