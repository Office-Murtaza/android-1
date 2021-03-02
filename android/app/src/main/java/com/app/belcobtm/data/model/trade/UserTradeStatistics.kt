package com.app.belcobtm.data.model.trade

data class UserTradeStatistics(
    val publicId: String,
    val status: Int,
    val totalTrades: Int,
    val tradingRate: Double
)