package com.belcobtm.data.rest.trade.response

import com.belcobtm.data.model.trade.TraderStatus
import com.belcobtm.data.websockets.chat.model.ChatMessageResponse

data class TradesResponse(
    val makerUsername: String?,
    @TraderStatus val makerStatus: Int,
    val makerTradeTotal: Int,
    val makerTradeRate: Double,
    val trades: List<TradeItemResponse>,
    val orders: List<TradeOrderItemResponse>,
    val messages: List<ChatMessageResponse>
)