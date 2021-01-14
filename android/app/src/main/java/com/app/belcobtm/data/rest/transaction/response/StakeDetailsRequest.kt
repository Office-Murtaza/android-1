package com.app.belcobtm.data.rest.transaction.response

import com.app.belcobtm.domain.transaction.item.StakeDetailsDataItem

class StakeDetailsRequest(
    @StakeDetailsStatus val status: Int,
    val amount: Double,
    val cancelDate: String?,
    val cancelHoldPeriod: Int,
    val createDate: String?,
    val duration: Int?,
    val rewardAmount: Double?,
    val rewardAnnualAmount: Double?,
    val rewardAnnualPercent: Double?,
    val rewardPercent: Double,
    val untilWithdraw: Int
)

fun StakeDetailsRequest.mapToDataItem(): StakeDetailsDataItem = StakeDetailsDataItem(
    status = status,
    amount = amount,
    rewardsAnnualAmount = rewardAnnualAmount,
    rewardsAnnualPercent = rewardAnnualPercent,
    rewardsAmount = rewardAmount,
    rewardsPercent = rewardPercent,
    createDate = createDate,
    cancelDate = cancelDate,
    duration = duration,
    cancelHoldPeriod = cancelHoldPeriod,
    untilWithdraw = untilWithdraw
)