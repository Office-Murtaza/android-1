package com.app.belcobtm.data.rest.transaction.response

import androidx.annotation.IntDef

@IntDef
@Retention(AnnotationRetention.SOURCE)
annotation class StakeDetailsStatus {
    companion object {
        const val NOT_EXIST = 1
        const val CREATE_PENDING = 2
        const val CREATED = 3
        const val CANCEL_PENDING = 4
        const val CANCEL = 5
        const val WITHDRAW_PENDING = 6
        const val WITHDRAWN = 7
    }
}
