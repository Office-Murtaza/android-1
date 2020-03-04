package com.app.belcobtm.domain.settings.item

import com.app.belcobtm.domain.settings.type.VerificationStatusType

data class VerificationInfoDataItem(
    val status: VerificationStatusType,
    val txLimit: Int,
    val dayLimit: Int,
    val message: String
)