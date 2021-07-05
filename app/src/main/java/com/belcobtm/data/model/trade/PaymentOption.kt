package com.belcobtm.data.model.trade

import androidx.annotation.IntDef
import com.belcobtm.data.model.trade.PaymentOption.Companion.CASH
import com.belcobtm.data.model.trade.PaymentOption.Companion.CASH_APP
import com.belcobtm.data.model.trade.PaymentOption.Companion.PAYONEER
import com.belcobtm.data.model.trade.PaymentOption.Companion.PAYPAL
import com.belcobtm.data.model.trade.PaymentOption.Companion.VENMO

@Target(AnnotationTarget.TYPE, AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
@IntDef(CASH, PAYPAL, VENMO, CASH_APP, PAYONEER)
annotation class PaymentOption {
    companion object {
        const val CASH = 1
        const val PAYPAL = 2
        const val VENMO = 3
        const val CASH_APP = 4
        const val PAYONEER = 5
    }
}