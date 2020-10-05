package com.app.belcobtm.presentation.features.wallet.staking

import com.app.belcobtm.domain.transaction.type.StakeStatus

data class StakingScreenItem(
    val price: Double,
    val balanceCoin: Double,
    val balanceUsd: Double,
    val amount: Double?,
    val status: StakeStatus,
    val rewardsAmount: Double?,
    val rewardsAmountAnnual: Double,
    val rewardsPercent: Double?,
    val rewardsPercentAnnual: Double,
    val createDate: String?,
    val cancelDate: String?,
    val duration: Int?,
    val cancelPeriod: Int,
    val untilWithdraw: Int?
)