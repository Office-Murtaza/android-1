package com.app.belcobtm.data.rest.transaction.response

import com.app.belcobtm.domain.transaction.item.TradeInfoDataItem

data class TradeInfoResponse(
    val total: Int?,
    val trades: List<TradeItemResponse>?
)

fun TradeInfoResponse.mapToDataItem(): TradeInfoDataItem = TradeInfoDataItem(
    total = total ?: 0,
    tradeList = trades?.map { it.mapToDataItem() } ?: emptyList()
)