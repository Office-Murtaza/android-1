package com.belcobtm.data.rest.settings.response

data class VerificationInfoResponse(
    val status: Int,
    val txLimit: Double,
    val dailyLimit: Double,
    val idCardNumberFilename: String?,
    val idCardNumber: String?,
    val firstName: String?,
    val lastName: String?,
    val address: String?,
    val city: String?,
    val country: String?,
    val province: String?,
    val zipCode: String?,
    val message: String?
)