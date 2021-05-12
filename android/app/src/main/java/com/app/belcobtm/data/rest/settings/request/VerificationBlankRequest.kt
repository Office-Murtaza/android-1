package com.app.belcobtm.data.rest.settings.request

data class VerificationBlankRequest(
    val file: String,
    val idNumber: String,
    val firstName: String,
    val lastName: String,
    val address: String,
    val city: String,
    val country: String,
    val province: String,
    val zipCode: String,
    val tierId: Int = VERIFICATION
) {
    companion object {
        const val VERIFICATION = 1
        const val VIP_VERIFICATION = 2
    }
}