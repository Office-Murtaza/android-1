package com.app.belcobtm.data.rest.transaction.response

import com.app.belcobtm.domain.transaction.item.StakeDetailsDataItem

class StakeDetailsRequest(
    val exist: Boolean,
    val unstakeAvailable: Boolean,
    val stakedDays: Int,
    val stakedAmount: Double,
    val rewardsAmount: Double,
    val rewardsAnnualAmount: Double,
    val rewardsPercent: Double,
    val rewardsAnnualPercent: Double,
    val stakingMinDays: Int
)

fun StakeDetailsRequest.mapToDataItem(): StakeDetailsDataItem = StakeDetailsDataItem(
    exist = exist,
    isUnStakeAvailable = unstakeAvailable,
    stakedDays = stakedDays,
    stakedAmount = stakedAmount,
    rewardsAnnualAmount = rewardsAnnualAmount,
    rewardsAnnualPercent = rewardsAnnualPercent,
    rewardsAmount = rewardsAmount,
    rewardsPercent = rewardsPercent,
    stakingMinDays = stakingMinDays
)