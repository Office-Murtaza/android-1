package com.app.belcobtm.data.model.trade

import androidx.annotation.IntDef

@Target(AnnotationTarget.TYPE, AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
@IntDef(
    OrderStatus.NEW, OrderStatus.CANCELLED, OrderStatus.DOING,
    OrderStatus.PAID, OrderStatus. RELEASED, OrderStatus.DISPUTING, OrderStatus.SOLVED
)
annotation class OrderStatus {
    companion object {
        const val NEW = 1
        const val CANCELLED = 2
        const val DOING = 3
        const val PAID = 4
        const val RELEASED = 5
        const val DISPUTING = 6
        const val SOLVED = 7
    }
}