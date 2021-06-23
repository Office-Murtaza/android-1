package com.app.belcobtm.data.model.trade

import com.app.belcobtm.presentation.features.wallet.trade.order.chat.model.ChatMessageItem

data class Order(
    val id: String,
    val tradeId: String,
    val coinCode: String,
    @OrderStatus val status: Int,
    val timestamp: Long,
    val price: Double,
    val cryptoAmount: Double,
    val fiatAmount: Double,
    val terms: String,
    val makerId: String,
    @TraderStatus val makerStatusId: Int,
    val makerRate: Double?,
    val makerPublicId: String,
    val makerLatitude: Double?,
    val makerLongitude: Double?,
    val makerTotalTrades: Int,
    val makerTradingRate: Double?,
    val takerId: String,
    @TraderStatus val takerStatusId: Int,
    val takerRate: Double?,
    val takerPublicId: String,
    val takerLatitude: Double?,
    val takerLongitude: Double?,
    val takerTotalTrades: Int,
    val takerTradingRate: Double?,
    val chatHistory: List<ChatMessageItem>
)