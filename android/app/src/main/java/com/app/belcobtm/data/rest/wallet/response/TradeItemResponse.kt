package com.app.belcobtm.data.rest.wallet.response

import com.app.belcobtm.domain.wallet.item.TradeDataItem

data class TradeItemResponse(
    val id: Int,
    val index: Int,
    val tradeCount: Int,
    val minLimit: Int,
    val maxLimit: Int,
    val rate: Int,
    val distance: Int,
    val price: Double,
    val publicId: String,
    val paymentMethod: String
)

fun TradeItemResponse.mapToDataItem(): TradeDataItem = TradeDataItem(
    id = id,
    index = index,
    tradeCount = tradeCount,
    minLimit = minLimit,
    maxLimit = maxLimit,
    rate = rate,
    distance = distance,
    price = price,
    userName = publicId,
    paymentMethod = paymentMethod
)