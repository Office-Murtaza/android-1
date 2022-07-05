package com.belcobtm.data.websockets.services.model

import com.belcobtm.data.disk.database.service.ServiceEntity
import com.belcobtm.domain.service.ServiceType

data class ServicesInfoResponse(
    val index: Int,
    val type: String,
    val locationEnabled: Boolean,
    val verificationEnabled: Boolean,
    val feePercent: Double,
    val txLimit: Double,
    val dailyLimit: Double,
    val remainLimit: Double,
) {

    fun toEntity(): ServiceEntity? =
        ServiceType.values().firstOrNull { it.name == type }?.let {
            ServiceEntity(
                id = it.value,
                locationEnabled = locationEnabled,
                verificationEnabled = verificationEnabled,
                feePercent = feePercent,
                txLimit = txLimit,
                dailyLimit = dailyLimit,
                remainLimit = remainLimit,
            )
        }

}
