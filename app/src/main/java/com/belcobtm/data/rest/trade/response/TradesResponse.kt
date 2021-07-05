package com.belcobtm.data.rest.trade.response

import com.belcobtm.data.model.trade.TraderStatus
import com.belcobtm.data.websockets.chat.model.ChatMessageResponse

data class TradesResponse(
    val makerPublicId: String,
    @TraderStatus val makerStatus: Int,
    val makerTotalTrades: Int,
    val makerTradingRate: Double,
    val trades: List<TradeItemResponse>,
    val orders: List<TradeOrderItemResponse>,
    val messages: List<ChatMessageResponse>
)