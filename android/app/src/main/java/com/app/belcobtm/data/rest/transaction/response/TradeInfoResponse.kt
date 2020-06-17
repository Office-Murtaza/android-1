package com.app.belcobtm.data.rest.transaction.response

import com.app.belcobtm.domain.transaction.item.TradeDataItem
import com.app.belcobtm.domain.transaction.item.TradeInfoDataItem

data class TradeInfoResponse(
    val total: Int?,
    val trades: List<TradeItemResponse>?
)

data class TradeItemResponse(
    val id: Int,
    val index: Int,
    val minLimit: Int,
    val maxLimit: Int,
    val rate: Int,
    val price: Double,
    val paymentMethod: String,
    val terms: String,
    val trader: TraderItemResponse?
)

data class TraderItemResponse(
    val username: String,
    val tradeCount: Int,
    val tradeRate: Double,
    val distance: Int
)

fun TradeInfoResponse.mapToDataItem(): TradeInfoDataItem = TradeInfoDataItem(
    total = total ?: 0,
    tradeList = trades?.map { it.mapToDataItem() } ?: emptyList()
)

fun TradeItemResponse.mapToDataItem(): TradeDataItem = TradeDataItem(
    id = id,
    index = index,
    minLimit = minLimit,
    maxLimit = maxLimit,
    price = price,
    paymentMethod = paymentMethod,
    terms = terms,
    userName = trader?.username ?: "",
    tradeCount = trader?.tradeCount ?: 0,
    rate = trader?.tradeRate ?: 0.0,
    distance = trader?.distance ?: 0
)