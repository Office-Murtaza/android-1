package com.belcobtm.domain.wallet.item

class ChartInfoListDataItem(
    val day: Pair<Double, List<Double>>,
    val week: Pair<Double, List<Double>>,
    val month: Pair<Double, List<Double>>,
    val threeMonths: Pair<Double, List<Double>>,
    val year: Pair<Double, List<Double>>
)