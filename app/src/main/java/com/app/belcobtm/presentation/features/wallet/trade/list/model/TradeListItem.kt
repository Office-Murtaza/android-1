package com.app.belcobtm.presentation.features.wallet.trade.list.model

data class TradeListItem(
    val buyTrades: List<TradeItem>,
    val sellTrades: List<TradeItem>,
    val orders: List<OrderItem>,
    val statistics: TradeStatistics
)