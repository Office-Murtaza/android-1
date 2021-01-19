package com.app.belcobtm.data.rest.transaction.response

import com.app.belcobtm.domain.transaction.item.StakeDetailsDataItem

class StakeDetailsResponse(
    @StakeDetailsStatus val status: Int,
    val amount: Double?,
    val rewardAmount: Double?,
    val rewardPercent: Double?,
    val rewardAnnualAmount: Double?,
    val holdPeriod: Int,
    val annualPercent: Double,
    val createDate: String?,
    val cancelDate: String?,
    val duration: Int?,
    val tillWithdrawal: Int?
)

fun StakeDetailsResponse.mapToDataItem(): StakeDetailsDataItem = StakeDetailsDataItem(
    status = status,
    amount = amount,
    rewardsAnnualAmount = rewardAnnualAmount,
    rewardsAnnualPercent = annualPercent,
    rewardsAmount = rewardAmount,
    rewardsPercent = rewardPercent,
    createDate = createDate,
    cancelDate = cancelDate,
    duration = duration,
    cancelHoldPeriod = holdPeriod,
    untilWithdraw = tillWithdrawal
)
