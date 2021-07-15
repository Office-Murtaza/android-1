package com.belcobtm.data.rest.atm.response

import androidx.annotation.IntDef

@Target(AnnotationTarget.TYPE, AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
@IntDef(OperationType.BUY_ONLY, OperationType.BUY_AND_SELL_ONLY)
annotation class OperationType {
    companion object {
        const val BUY_ONLY = 1
        const val BUY_AND_SELL_ONLY = 2
    }
}