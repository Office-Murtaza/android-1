package com.app.belcobtm.presentation.features.wallet.transactions.item

import com.app.belcobtm.data.rest.wallet.request.PriceChartPeriod
import com.app.belcobtm.domain.wallet.item.ChartDataItem

data class CurrentChartInfo(
    @PriceChartPeriod val period: Int,
    val chartInfo: ChartDataItem
)