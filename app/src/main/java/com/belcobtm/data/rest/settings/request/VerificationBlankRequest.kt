package com.belcobtm.data.rest.settings.request

data class VerificationBlankRequest(
    val idCardNumberFilename: String,
    val idCardNumber: String,
    val idCardNumberMimetype: String,
    val firstName: String,
    val lastName: String,
    val address: String,
    val city: String,
    val country: String,
    val province: String,
    val zipCode: String,
    val tier: Int = VERIFICATION
) {
    companion object {
        const val VERIFICATION = 1
        const val VIP_VERIFICATION = 2
    }
}