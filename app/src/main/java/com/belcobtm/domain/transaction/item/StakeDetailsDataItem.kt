package com.belcobtm.domain.transaction.item

import com.belcobtm.data.rest.transaction.response.StakeDetailsStatus

class StakeDetailsDataItem(
        @StakeDetailsStatus val status: Int,
        val amount: Double?,
        val rewardsAmount: Double?,
        val rewardsAnnualAmount: Double?,
        val rewardsPercent: Double?,
        val rewardsAnnualPercent: Double?,
        val createTimestamp: Long?,
        val cancelTimestamp: Long?,
        val duration: Int?,
        val cancelHoldPeriod: Int,
        val untilWithdraw: Int?
)