package com.app.belcobtm.data.rest.transaction.response

import com.app.belcobtm.domain.transaction.item.StakeDetailsDataItem

class StakeDetailsRequest(
    val exist: Boolean,
    val stakedDays: Int,
    val stakedAmount: Double,
    val rewardsAmount: Double,
    val rewardsPercent: Double
)

fun StakeDetailsRequest.mapToDataItem(): StakeDetailsDataItem = StakeDetailsDataItem(
    exist = exist,
    stakedDays = stakedDays,
    stakedAmount = stakedAmount,
    rewardsAmount = rewardsAmount,
    rewardsPercent = rewardsPercent
)