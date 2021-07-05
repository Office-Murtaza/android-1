package com.belcobtm.data.model.trade

data class UserTradeStatistics(
    val publicId: String,
    @TraderStatus val status: Int,
    val totalTrades: Int,
    val tradingRate: Double
)