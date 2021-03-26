package com.app.belcobtm.data.rest.trade.response

import com.app.belcobtm.data.model.trade.TraderStatus

data class TradesResponse(
    val publicId: String,
    @TraderStatus val status: Int,
    val totalTrades: Int,
    val tradingRate: Double,
    val trades: List<TradeItemResponse>,
    val orders: List<TradeOrderItemResponse>
)