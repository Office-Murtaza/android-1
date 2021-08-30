package com.belcobtm.domain.transaction.item

data class SellLimitsDataItem(
    val dailyLimit: Double,
    val txLimit: Double,
    val todayLimit: Double
)