package com.app.belcobtm.data.rest.wallet.response

import com.app.belcobtm.domain.wallet.item.ChartDataItem
import com.app.belcobtm.domain.wallet.item.ChartInfoListDataItem

data class ChartResponse(
    val price: Double,
    val balance: Double,
    val chart: ChartInfoListResponse
)

data class ChartInfoListResponse(
    val day: ChartInfoResponse,
    val week: ChartInfoResponse,
    val month: ChartInfoResponse,
    val threeMonths: ChartInfoResponse,
    val year: ChartInfoResponse
)

data class ChartInfoResponse(
    val prices: List<Double>,
    val changes: Double
)

fun ChartResponse.mapToDataItem(): ChartDataItem = ChartDataItem(
    price = price,
    balance = balance,
    chart = chart.mapToDataItem()
)

private fun ChartInfoListResponse.mapToDataItem(): ChartInfoListDataItem = ChartInfoListDataItem(
    day = Pair(day.changes, day.prices),
    week = Pair(week.changes, week.prices),
    month = Pair(month.changes, month.prices),
    threeMonths = Pair(threeMonths.changes, threeMonths.prices),
    year = Pair(year.changes, year.prices)
)