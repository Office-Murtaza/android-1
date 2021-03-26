package com.app.belcobtm.data.model.trade

import androidx.annotation.IntDef

@Target(AnnotationTarget.TYPE, AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
@IntDef(TraderStatus.NOT_VERIFIED, TraderStatus.VERIFIED, TraderStatus.VIP_VERIFIED)
annotation class TraderStatus {
    companion object {
        const val NOT_VERIFIED = 1
        const val VERIFIED = 4
        const val VIP_VERIFIED = 7
    }
}