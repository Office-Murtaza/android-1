package com.belcobtm.presentation.features.wallet.transactions.item

import com.belcobtm.data.rest.wallet.request.PriceChartPeriod
import com.belcobtm.domain.wallet.item.ChartDataItem

data class CurrentChartInfo(
    val period: PriceChartPeriod,
    val chartInfo: ChartDataItem
)
