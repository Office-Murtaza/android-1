package com.belcobtm.domain.trade.model

data class UserTradeStatsDomainModel(
    val publicId: String,
    val totalTrades: Int,
    val tradingRate: Double
)