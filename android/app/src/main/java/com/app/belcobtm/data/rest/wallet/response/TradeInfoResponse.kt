package com.app.belcobtm.data.rest.wallet.response

import com.app.belcobtm.domain.wallet.item.TradeInfoDataItem

data class TradeInfoResponse(
    val buyTotal: Int?,
    val sellTotal: Int?,
    val openTotal: Int?,
    val buyTrades: List<TradeItemResponse>?,
    val sellTrades: List<TradeItemResponse>?,
    val openTrades: List<TradeItemResponse>?
)

fun TradeInfoResponse.mapToDataItem(): TradeInfoDataItem = TradeInfoDataItem(
    buyTotal = buyTotal ?: 0,
    sellTotal = sellTotal ?: 0,
    openTotal = openTotal ?: 0,
    buyTrades = buyTrades?.map { it.mapToDataItem() } ?: emptyList(),
    sellTrades = sellTrades?.map { it.mapToDataItem() } ?: emptyList(),
    openTrades = openTrades?.map { it.mapToDataItem() } ?: emptyList()
)