package com.app.belcobtm.presentation.features.wallet.trade.list.model

data class TradeStatistics(
    val publicId: String,
    val status: Int,
    val totalTrades: Int,
    val tradingRate: Double
)