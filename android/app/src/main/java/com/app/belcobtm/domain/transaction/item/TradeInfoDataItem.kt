package com.app.belcobtm.domain.transaction.item

data class TradeInfoDataItem(
    val total: Int,
    val tradeList: List<TradeDataItem>
)