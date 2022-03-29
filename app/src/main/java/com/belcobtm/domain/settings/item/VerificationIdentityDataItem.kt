package com.belcobtm.domain.settings.item

data class VerificationIdentityDataItem(
    val countryCode: String,
    val firstName: String,
    val lastName: String,
    val dayOfBirth: Int,
    val monthOfBirth: Int,
    val yearOfBirth: Int,
    val province: String,
    val city: String,
    val streetName: String,
    val buildingNumber: String,
    val zipCode: String,
    val ssn: String,
)
