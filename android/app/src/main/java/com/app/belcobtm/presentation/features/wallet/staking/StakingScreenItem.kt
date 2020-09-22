package com.app.belcobtm.presentation.features.wallet.staking

data class StakingScreenItem(
    val isExist: Boolean,
    val isUnStakeAvailable: Boolean,
    val price: Double,
    val balanceCoin: Double,
    val balanceUsd: Double,
    val staked: Double,
    val rewardsAmount: Double,
    val rewardsAmountAnnual: Double,
    val rewardsPercent: Double,
    val rewardsPercentAnnual: Double,
    val time: Int,
    val stakingMinDays: Int
)