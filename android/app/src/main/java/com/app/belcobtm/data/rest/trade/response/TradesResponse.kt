package com.app.belcobtm.data.rest.trade.response

data class TradesResponse(
    val publicId: String,
    val status: Int,
    val totalTrades: Int,
    val tradingRate: Double,
    val trades: List<TradeItemResponse>,
    val orders: List<TradeOrderItemResponse>
)