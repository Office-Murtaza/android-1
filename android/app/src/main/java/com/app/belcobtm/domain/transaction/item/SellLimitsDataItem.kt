package com.app.belcobtm.domain.transaction.item

data class SellLimitsDataItem(
    val usdDailyLimit: Double,
    val usdTxLimit: Double,
    val profitRate: Double
)