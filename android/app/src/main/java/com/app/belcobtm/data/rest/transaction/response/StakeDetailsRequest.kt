package com.app.belcobtm.data.rest.transaction.response

import com.app.belcobtm.domain.transaction.item.StakeDetailsDataItem

class StakeDetailsRequest(
    @StakeDetailsStatus val status: Int,
    val amount: Double,
    val amountStr: String,
    val cancelDate: String?,
    val cancelPeriod: Int,
    val createDate: String?,
    val duration: Int?,
    val rewardAmount: Double?,
    val rewardAmountStr: String,
    val rewardAnnualAmount: Double?,
    val rewardAnnualAmountStr: String,
    val rewardAnnualPercent: Double?,
    val rewardAnnualPercentStr: String,
    val rewardPercent: Double,
    val rewardPercentStr: String,
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
    cancelPeriod = cancelPeriod,
    untilWithdraw = untilWithdraw
)