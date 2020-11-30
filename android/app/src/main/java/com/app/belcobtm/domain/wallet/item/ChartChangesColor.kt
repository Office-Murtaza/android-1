package com.app.belcobtm.domain.wallet.item

import androidx.annotation.IntDef
import com.app.belcobtm.domain.wallet.item.ChartChangesColor.Companion.BLACK
import com.app.belcobtm.domain.wallet.item.ChartChangesColor.Companion.GREEN
import com.app.belcobtm.domain.wallet.item.ChartChangesColor.Companion.RED

@Retention(AnnotationRetention.SOURCE)
@IntDef(GREEN, RED, BLACK)
annotation class ChartChangesColor {
    companion object {
        const val GREEN = 1
        const val RED = 2
        const val BLACK = 3
    }
}