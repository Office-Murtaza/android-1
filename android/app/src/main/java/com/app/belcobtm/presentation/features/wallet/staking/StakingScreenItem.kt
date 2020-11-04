package com.app.belcobtm.presentation.features.wallet.staking

import com.app.belcobtm.data.rest.transaction.response.StakeDetailsStatus

data class StakingScreenItem(
        val price: Double,
        val balanceCoin: Double,
        val balanceUsd: Double,
        val amount: Double?,
        @StakeDetailsStatus val status: Int,
        val rewardsAmount: Double?,
        val rewardsAmountAnnual: Double?,
        val rewardsPercent: Double?,
        val rewardsPercentAnnual: Double?,
        val createDate: String?,
        val cancelDate: String?,
        val duration: Int?,
        val holdPeriod: Int,
        val untilWithdraw: Int?
)