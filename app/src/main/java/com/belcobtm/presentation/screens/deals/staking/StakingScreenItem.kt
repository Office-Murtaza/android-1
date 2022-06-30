package com.belcobtm.presentation.screens.deals.staking

import com.belcobtm.data.rest.transaction.response.StakeDetailsStatus

data class StakingScreenItem(
    val price: Double,
    val balanceCoin: Double,
    val balanceUsd: Double,
    val ethFee: Double,
    val reservedCode: String,
    val reservedBalanceCoin: Double,
    val reservedBalanceUsd: Double,
    val amount: Double?,
    @StakeDetailsStatus val status: Int,
    val rewardsAmount: Double?,
    val rewardsAmountAnnual: Double?,
    val rewardsPercent: Double?,
    val rewardsPercentAnnual: Double?,
    val createDate: String?,
    val cancelDate: String?,
    val duration: Int?,
    val cancelHoldPeriod: Int,
    val untilWithdraw: Int?
)