package com.belcobtm.data.websockets.services.model

import com.belcobtm.data.disk.database.service.ServiceEntity
import com.belcobtm.data.disk.database.service.ServiceType

//{
//    "type": "TRADE",
//    "enabled": true,
//    "feePercentage": 1,
//    "txLimit": 100,
//    "dailyLimit": 1000,
//    "remainLimit": 800
//},
data class ServicesInfoResponse(
    val type: String,
    val enabled: Boolean,
    val feePercentage: Double,
    val txLimit: Double,
    val dailyLimit: Double,
    val remainLimit: Double,
) {
    companion object {
        const val TRADE = "TRADE"
        const val TRANSFER = "TRANSFER"
        const val ATM_SELL = "ATM_SELL"
        const val SWAP = "SWAP"
        const val STAKING = "STAKING"
    }
}

fun ServicesInfoResponse.toEntity(): ServiceEntity? {
    val id = when (type) {
        ServicesInfoResponse.TRADE -> ServiceType.TRADE
        ServicesInfoResponse.SWAP -> ServiceType.SWAP
        ServicesInfoResponse.ATM_SELL -> ServiceType.ATM_SELL
        ServicesInfoResponse.TRANSFER -> ServiceType.TRANSFER
        ServicesInfoResponse.STAKING -> ServiceType.STAKING
        else -> return null
    }
    return ServiceEntity(
        id = id,
        feePercent = feePercentage,
        txLimit = txLimit,
        dailyLimit = dailyLimit,
        remainLimit = remainLimit,
    )
}