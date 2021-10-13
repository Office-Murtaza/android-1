package com.belcobtm.data.rest.settings.request

import com.belcobtm.domain.settings.type.VerificationStatus

data class VipVerificationRequest(
    val idCardNumberFilename: String,
    val idCardNumber: String,
    val firstName: String,
    val lastName: String,
    val address: String,
    val city: String,
    val country: String,
    val province: String,
    val zipCode: String,
    val snn: String,
    val ssnFilename: String,
    val staus: Int = VerificationStatus.VIP_VERIFICATION_PENDING.code
)