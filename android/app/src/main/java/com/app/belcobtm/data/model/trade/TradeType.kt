package com.app.belcobtm.data.model.trade

import androidx.annotation.IntDef
import com.app.belcobtm.data.model.trade.TradeType.Companion.BUY
import com.app.belcobtm.data.model.trade.TradeType.Companion.SELL

@IntDef(BUY, SELL)
@Retention(AnnotationRetention.SOURCE)
annotation class TradeType {
    companion object {
        const val BUY = 1
        const val SELL = 2
    }
}