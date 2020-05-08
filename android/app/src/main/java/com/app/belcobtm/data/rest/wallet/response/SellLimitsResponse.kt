package com.app.belcobtm.data.rest.wallet.response

import com.app.belcobtm.domain.wallet.item.SellLimitsDataItem
import java.io.Serializable

data class LimitsResponse(
    val dailyLimit: Limit?,
    val txLimit: Limit?,
    val sellProfitRate: Double
)

data class Limit(
    val USD: Double?
) : Serializable

fun LimitsResponse.mapToDataItem(): SellLimitsDataItem = SellLimitsDataItem(
    usdDailyLimit = dailyLimit?.USD ?: 0.0,
    usdTxLimit = dailyLimit?.USD ?: 0.0,
    profitRate = sellProfitRate
)