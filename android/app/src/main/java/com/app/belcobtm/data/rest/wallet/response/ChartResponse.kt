package com.app.belcobtm.data.rest.wallet.response

import com.app.belcobtm.domain.wallet.item.ChartChangesColor
import com.app.belcobtm.domain.wallet.item.ChartDataItem
import com.github.mikephil.charting.data.BarEntry

data class ChartResponse(
    val prices: List<List<Double>>
)

fun ChartResponse.mapToDataItem(): ChartDataItem =
    prices.map { it.last() }.let { prices ->
        val changes = prices.takeIf(List<Double>::isNotEmpty)
            ?.let {
                (prices.last() - prices.first()) / prices.first() * 100
            } ?: 0.0
        ChartDataItem(
            prices = prices.mapIndexed { index, value ->
                BarEntry(index.toFloat(), value.toFloat())
            },
            circles = prices.takeIf(List<Double>::isNotEmpty)
                ?.last()
                ?.let { lastPrice ->
                    listOf(BarEntry((prices.size - 1).toFloat(), lastPrice.toFloat()))
                }.orEmpty(),
            changes = changes,
            changesColor = if (changes >= 0) ChartChangesColor.GREEN else ChartChangesColor.RED
        )
    }