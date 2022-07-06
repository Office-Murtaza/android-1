package com.belcobtm.data.rest.settings.request

import com.belcobtm.domain.settings.type.VerificationStatus

data class VerificationBlankRequest(
    val id: String?,
    val idCardNumberFilename: String,
    val idCardNumber: String,
    val firstName: String,
    val lastName: String,
    val address: String,
    val city: String,
    val country: String,
    val province: String,
    val zipCode: String,
    val status: String = VerificationStatus.PENDING.stringValue
)