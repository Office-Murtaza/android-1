package com.app.belcobtm.domain.settings.item

import com.app.belcobtm.domain.settings.type.VerificationStatus

data class VerificationInfoDataItem(
    val status: VerificationStatus,
    val txLimit: Double,
    val dayLimit: Double,
    val message: String
)