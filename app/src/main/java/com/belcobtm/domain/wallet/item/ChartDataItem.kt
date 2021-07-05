package com.belcobtm.domain.wallet.item

import com.github.mikephil.charting.data.BarEntry

class ChartDataItem(
    @ChartChangesColor val changesColor: Int,
    val changes: Double,
    val prices: List<BarEntry>,
    val circles: List<BarEntry>
)