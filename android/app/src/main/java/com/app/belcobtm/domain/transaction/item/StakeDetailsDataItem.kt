package com.app.belcobtm.domain.transaction.item

import com.app.belcobtm.data.rest.transaction.response.StakeDetailsStatus

class StakeDetailsDataItem(
        @StakeDetailsStatus val status: Int,
        val amount: Double?,
        val rewardsAmount: Double?,
        val rewardsAnnualAmount: Double?,
        val rewardsPercent: Double?,
        val rewardsAnnualPercent: Double?,
        val createDate: String?,
        val cancelDate: String?,
        val duration: Int?,
        val holdPeriod: Int,
        val untilWithdraw: Int?
)