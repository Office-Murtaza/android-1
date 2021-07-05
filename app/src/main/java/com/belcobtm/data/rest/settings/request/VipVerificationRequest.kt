package com.belcobtm.data.rest.settings.request

import com.belcobtm.data.rest.settings.request.VerificationBlankRequest.Companion.VIP_VERIFICATION

data class VipVerificationRequest(
    val snn: String,
    val ssnFilename: String,
    val ssnMimetype: String,
    val tier: Int = VIP_VERIFICATION
)