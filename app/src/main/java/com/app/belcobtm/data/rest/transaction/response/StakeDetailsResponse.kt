package com.app.belcobtm.data.rest.transaction.response

import com.app.belcobtm.domain.transaction.item.StakeDetailsDataItem
import kotlin.math.max

class StakeDetailsResponse(
    val id: String,
    val coinId: String,
    @StakeDetailsStatus val status: Int,
    val cryptoAmount: Double?,
    val basePeriod: Int,
    val annualPeriod: Int,
    val holdPeriod: Int,
    val annualPercent: Double,
    val createTxId: String?,
    val createTimestamp: Long,
    val cancelTxId: String?,
    val cancelTimestamp: Long?,
    val withdrawTxId: String?,
    val withdrawTimestamp: Long?
)

fun StakeDetailsResponse.mapToDataItem(): StakeDetailsDataItem {
    val endTime = cancelTimestamp ?: System.currentTimeMillis()
    val duration = (endTime - createTimestamp) / (basePeriod * 1000)
    val rewardsPercent = duration * (annualPercent / annualPeriod / basePeriod)
    return StakeDetailsDataItem(
        status = status,
        amount = cryptoAmount,
        rewardsPercent = rewardsPercent,
        rewardsAmount = (cryptoAmount ?: 0.0) * rewardsPercent / 100,
        rewardsAnnualAmount = (cryptoAmount ?: 0.0) * annualPercent / 100,
        rewardsAnnualPercent = annualPercent,
        createTimestamp = createTimestamp,
        cancelTimestamp = cancelTimestamp,
        duration = duration.toInt(),
        cancelHoldPeriod = holdPeriod / basePeriod,
        untilWithdraw = max(0, (holdPeriod - ((cancelTimestamp ?: 0) - createTimestamp) / 1000) / basePeriod).toInt()
    )
}