package com.app.belcobtm.domain.transaction.item

class StakeDetailsDataItem(
    val exist: Boolean,
    val stakedDays: Int,
    val stakedAmount: Double,
    val rewardsAmount: Double,
    val rewardsPercent: Double
)