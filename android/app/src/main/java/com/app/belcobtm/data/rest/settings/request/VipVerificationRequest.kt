package com.app.belcobtm.data.rest.settings.request

import com.app.belcobtm.data.rest.settings.request.VerificationBlankRequest.Companion.VIP_VERIFICATION

data class VipVerificationRequest(
    val snn: String,
    val ssnFilename: String,
    val tier: Int = VIP_VERIFICATION
)