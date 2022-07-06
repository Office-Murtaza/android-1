package com.belcobtm.domain.trade.model.order

import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.presentation.screens.wallet.trade.order.chat.model.ChatMessageItem

data class OrderDomainModel(
    val id: String,
    val tradeId: String,
    val coin: LocalCoinType,
    val status: OrderStatus,
    val cryptoAmount: Double,
    val price: Double,
    val fiatAmount: Double,
    val feePercent: Double,
    val resolution: String,
    val takerRate: Int,
    val takerLatitude: Double,
    val takerLongitude: Double,

    val takerUserId: String,
    val takerUsername: String,
    val takerTradeTotal: Int,
    val takerTradeRate: Double,
    val makerUserId: String,
    val makerUsername: String,
    val makerRate: Int,
    val makerLatitude: Double,
    val makerLongitude: Double,
    val makerTradeTotal: Int,
    val makerTradeRate: Double,
    val timestamp: Long,
    val terms: String,
    val chatHistory: List<ChatMessageItem>
)
