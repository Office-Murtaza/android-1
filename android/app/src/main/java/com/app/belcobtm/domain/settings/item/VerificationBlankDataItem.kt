package com.app.belcobtm.domain.settings.item

data class VerificationBlankDataItem(
    val tierId: Int,
    val file: String,
    val idNumber: String,
    val snn: String,
    val firstName: String,
    val lastName: String,
    val address: String,
    val city: String,
    val country: String,
    val province: String,
    val zipCode: String
)