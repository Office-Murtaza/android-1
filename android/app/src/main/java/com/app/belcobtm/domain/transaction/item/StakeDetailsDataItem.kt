package com.app.belcobtm.domain.transaction.item

class StakeDetailsDataItem(
    val exist: Boolean,
    val isUnStakeAvailable: Boolean,
    val stakedDays: Int,
    val stakedAmount: Double,
    val rewardsAmount: Double,
    val rewardsAnnualAmount: Double,
    val rewardsPercent: Double,
    val rewardsAnnualPercent: Double,
    val stakingMinDays: Int
)