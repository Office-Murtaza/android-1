package com.app.belcobtm.data.rest.transaction.response

import com.app.belcobtm.domain.transaction.item.TradeDataItem

data class TradeItemResponse(
    val id: Int,
    val index: Int,
    val tradeCount: Int,
    val minLimit: Int,
    val maxLimit: Int,
    val rate: Int,
    val distance: Int,
    val price: Double,
    val tradeRate: Double,
    val username: String?,
    val paymentMethod: String,
    val terms: String
)

fun TradeItemResponse.mapToDataItem(): TradeDataItem = TradeDataItem(
    id = id,
    index = index,
    tradeCount = tradeCount,
    minLimit = minLimit,
    maxLimit = maxLimit,
    rate = tradeRate,
    distance = distance,
    price = price,
    userName = username ?: "",
    paymentMethod = paymentMethod,
    terms = terms
)