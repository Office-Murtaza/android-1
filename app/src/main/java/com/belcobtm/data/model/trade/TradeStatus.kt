package com.belcobtm.data.model.trade

import androidx.annotation.IntDef

@Target(AnnotationTarget.TYPE, AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
@IntDef(TradeStatus.ACTIVE, TradeStatus.CANCELLED, TradeStatus.DELETED)
annotation class TradeStatus {
    companion object {
        const val ACTIVE = 1
        const val CANCELLED = 2
        const val DELETED = 3
    }
}