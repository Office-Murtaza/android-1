package com.app.belcobtm.data.rest.settings.response

data class VerificationInfoResponse(
    val status: Int = 1,
    val txLimit: Int = 0,
    val dailyLimit: Int = 0,
    val message: String = ""
)