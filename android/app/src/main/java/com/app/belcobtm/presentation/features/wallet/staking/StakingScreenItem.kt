package com.app.belcobtm.presentation.features.wallet.staking

data class StakingScreenItem(
    val isExist: Boolean,
    val isUnStakeAvailable: Boolean,
    val price: Double,
    val balanceCoin: Double,
    val balanceUsd: Double,
    val staked: Double,
    val rewardsAmount: Double,
    val rewardsPercent: Double,
    val time: Int
)