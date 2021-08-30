package com.belcobtm.data.rest.transaction.response

import com.belcobtm.domain.transaction.item.SellLimitsDataItem
import java.io.Serializable

data class LimitsResponse(
    val dailyLimit: Double?,
    val txLimit: Double?,
    val todayLimit: Double?
)

fun LimitsResponse.mapToDataItem(): SellLimitsDataItem = SellLimitsDataItem(
    dailyLimit = dailyLimit ?: 0.0,
    txLimit = txLimit ?: 0.0,
    todayLimit = todayLimit ?: 0.0
)