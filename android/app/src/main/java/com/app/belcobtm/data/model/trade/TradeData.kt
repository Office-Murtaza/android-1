package com.app.belcobtm.data.model.trade

data class TradeData(
    val trades: MutableMap<Int, Trade>,
    val orders: MutableMap<Int, Order>,
    val statistics: UserTradeStatistics
)