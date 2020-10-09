package com.app.belcobtm.data.rest.transaction.response

import com.app.belcobtm.domain.transaction.item.StakeDetailsDataItem
import com.app.belcobtm.domain.transaction.type.StakeStatus

class StakeDetailsRequest(
    val amount: Double,
    val amountStr: String,
    val cancelDate: String?,
    val cancelPeriod: Int,
    val canceled: Boolean,
    val createDate: String?,
    val created: Boolean,
    val duration: Int?,
    val rewardAmount: Double?,
    val rewardAmountStr: String,
    val rewardAnnualAmount: Double?,
    val rewardAnnualAmountStr: String,
    val rewardAnnualPercent: Double?,
    val rewardAnnualPercentStr: String,
    val rewardPercent: Double,
    val rewardPercentStr: String,
    val untilWithdraw: Int,
    val withdrawn: Boolean
)

fun StakeDetailsRequest.mapToDataItem(): StakeDetailsDataItem = StakeDetailsDataItem(
    status = when {
        created && !canceled -> StakeStatus.CREATED
        canceled && !withdrawn  -> StakeStatus.CANCELED
        else -> StakeStatus.NONE
    },
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