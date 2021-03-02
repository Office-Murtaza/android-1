package com.app.belcobtm.data.model.trade

data class TradeData(
    val trades: List<Trade>,
    val orders: List<Order>,
    val statistics: UserTradeStatistics
)