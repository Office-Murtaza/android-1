package com.belcobtm.data.model.trade

import androidx.annotation.IntDef
import com.belcobtm.data.model.trade.TradeType.Companion.BUY
import com.belcobtm.data.model.trade.TradeType.Companion.SELL

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.TYPE)
@IntDef(BUY, SELL)
@Retention(AnnotationRetention.SOURCE)
annotation class TradeType {
    companion object {
        const val BUY = 1
        const val SELL = 2
    }
}