package com.app.belcobtm.domain.wallet.item

data class TradeInfoDataItem(
    val buyTotal: Int,
    val sellTotal: Int,
    val openTotal: Int,
    val buyTrades: List<TradeDataItem>,
    val sellTrades: List<TradeDataItem>,
    val openTrades: List<TradeDataItem>
)