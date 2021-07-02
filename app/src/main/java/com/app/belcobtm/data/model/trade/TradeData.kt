package com.app.belcobtm.data.model.trade

data class TradeData(
    val trades: MutableMap<String, Trade>,
    val orders: MutableMap<String, Order>,
    val statistics: UserTradeStatistics
)