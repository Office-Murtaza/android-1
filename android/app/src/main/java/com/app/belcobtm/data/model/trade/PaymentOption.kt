package com.app.belcobtm.data.model.trade

import androidx.annotation.IntDef
import com.app.belcobtm.data.model.trade.PaymentOption.Companion.CASH
import com.app.belcobtm.data.model.trade.PaymentOption.Companion.CASH_APP
import com.app.belcobtm.data.model.trade.PaymentOption.Companion.PAYONEER
import com.app.belcobtm.data.model.trade.PaymentOption.Companion.PAYPAL
import com.app.belcobtm.data.model.trade.PaymentOption.Companion.VENMO

@Target(AnnotationTarget.TYPE, AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
@IntDef(CASH, PAYPAL, VENMO, CASH_APP, PAYONEER)
annotation class PaymentOption {
    companion object {
        const val CASH = 0
        const val PAYPAL = 1
        const val VENMO = 2
        const val CASH_APP = 3
        const val PAYONEER = 4

    }
}