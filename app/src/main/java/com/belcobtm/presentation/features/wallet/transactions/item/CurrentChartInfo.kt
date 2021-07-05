package com.belcobtm.presentation.features.wallet.transactions.item

import com.belcobtm.data.rest.wallet.request.PriceChartPeriod
import com.belcobtm.domain.wallet.item.ChartDataItem

data class CurrentChartInfo(
    @PriceChartPeriod val period: Int,
    val chartInfo: ChartDataItem
)