package com.belcobtm.data.disk.database.service

import androidx.annotation.IntDef

@IntDef(
    ServiceType.TRANSFER,
    ServiceType.SWAP,
    ServiceType.STAKING,
    ServiceType.TRADE,
    ServiceType.ATM_SELL
)
@Retention(AnnotationRetention.SOURCE)
annotation class ServiceType {
    companion object {
        const val TRANSFER = 1
        const val SWAP = 2
        const val STAKING = 3
        const val TRADE = 4
        const val ATM_SELL = 5
    }
}