package com.app.belcobtm.domain.transaction.item

import com.app.belcobtm.domain.transaction.type.StakeStatus

class StakeDetailsDataItem(
    val status: StakeStatus,
    val amount: Double?,
    val rewardsAmount: Double?,
    val rewardsAnnualAmount: Double?,
    val rewardsPercent: Double?,
    val rewardsAnnualPercent: Double?,
    val createDate: String?,
    val cancelDate: String?,
    val duration: Int?,
    val cancelPeriod: Int,
    val untilWithdraw: Int?
)