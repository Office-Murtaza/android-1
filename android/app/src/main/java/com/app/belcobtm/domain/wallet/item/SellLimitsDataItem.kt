package com.app.belcobtm.domain.wallet.item

data class SellLimitsDataItem(
    val usdDailyLimit: Double,
    val usdTxLimit: Double,
    val profitRate: Double
)