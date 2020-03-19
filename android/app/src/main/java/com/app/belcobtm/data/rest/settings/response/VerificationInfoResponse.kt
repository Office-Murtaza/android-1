package com.app.belcobtm.data.rest.settings.response

data class VerificationInfoResponse(
    val status: Int,
    val txLimit: Double,
    val dailyLimit: Double,
    val message: String?
)