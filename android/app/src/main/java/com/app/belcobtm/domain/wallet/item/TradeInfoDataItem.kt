package com.app.belcobtm.domain.wallet.item

data class TradeInfoDataItem(
    val total: Int,
    val tradeList: List<TradeDataItem>
)