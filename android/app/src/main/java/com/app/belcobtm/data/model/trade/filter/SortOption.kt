package com.app.belcobtm.data.model.trade.filter

import androidx.annotation.IntDef
import com.app.belcobtm.data.model.trade.filter.SortOption.Companion.DISTANCE
import com.app.belcobtm.data.model.trade.filter.SortOption.Companion.PRICE

@Target(AnnotationTarget.TYPE, AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
@IntDef(PRICE, DISTANCE)
annotation class SortOption {
    companion object {
        const val PRICE = 1
        const val DISTANCE = 2
    }
}