package com.app.belcobtm.data.rest.wallet.request

import androidx.annotation.IntDef
import com.app.belcobtm.data.rest.wallet.request.PriceChartPeriod.Companion.PERIOD_DAY
import com.app.belcobtm.data.rest.wallet.request.PriceChartPeriod.Companion.PERIOD_MONTH
import com.app.belcobtm.data.rest.wallet.request.PriceChartPeriod.Companion.PERIOD_QUARTER
import com.app.belcobtm.data.rest.wallet.request.PriceChartPeriod.Companion.PERIOD_WEEK
import com.app.belcobtm.data.rest.wallet.request.PriceChartPeriod.Companion.PERIOD_YEAR

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.TYPE)
@Retention(AnnotationRetention.SOURCE)
@IntDef(PERIOD_DAY, PERIOD_WEEK, PERIOD_MONTH, PERIOD_QUARTER, PERIOD_YEAR)
annotation class PriceChartPeriod {
    companion object {
        const val PERIOD_DAY = 1
        const val PERIOD_WEEK = 2
        const val PERIOD_MONTH = 3
        const val PERIOD_QUARTER = 4
        const val PERIOD_YEAR = 5
    }
}