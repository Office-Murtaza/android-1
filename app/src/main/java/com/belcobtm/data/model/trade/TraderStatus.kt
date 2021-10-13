package com.belcobtm.data.model.trade

import androidx.annotation.IntDef

@Target(AnnotationTarget.TYPE, AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
@IntDef(
    TraderStatus.NOT_VERIFIED,
    TraderStatus.VERIFICATION_PENDING,
    TraderStatus.VERIFICATION_REJECTED,
    TraderStatus.VERIFIED,
    TraderStatus.VIP_VERIFICATION_PENDING,
    TraderStatus.VIP_VERIFICATION_REJECTED,
    TraderStatus.VIP_VERIFIED
)
annotation class TraderStatus {
    companion object {
        const val NOT_VERIFIED = 1
        const val VERIFICATION_PENDING = 2
        const val VERIFICATION_REJECTED = 3
        const val VERIFIED = 4
        const val VIP_VERIFICATION_PENDING = 5
        const val VIP_VERIFICATION_REJECTED = 6
        const val VIP_VERIFIED = 7
    }
}