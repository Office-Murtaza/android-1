package com.app.belcobtm.domain.transaction.item

class StakeDetailsDataItem(
    val created: Boolean,
    val canceled: Boolean,
    val withdrawn: Boolean,
    val amount: Double?,
    val rewardsAmount: Double?,
    val rewardsAnnualAmount: Double,
    val rewardsPercent: Double?,
    val rewardsAnnualPercent: Double,
    val createDate: String?,
    val cancelDate: String?,
    val duration: Int?,
    val cancelPeriod: Int,
    val untilWithdraw: Int?

)