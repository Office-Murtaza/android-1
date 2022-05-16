package com.belcobtm.data.rest.settings.request

data class VerificationUserIdentityRequest(
    val countryCode: String,
    val sourceOfFunds: String,
    val occupation: String,
    val verificationData: VerificationData,
)

data class VerificationData(
    val personInfo: PersonInfo,
    val location: LocationInfo,
    val communication: CommunicationInfo,
    val nationalIds: List<NationalId>
)

data class PersonInfo(
    val firstGivenName: String,
    val firstSurName: String,
    val dayOfBirth: Int,
    val monthOfBirth: Int,
    val yearOfBirth: Int
)

data class LocationInfo(
    val buildingNumber: String,
    val streetName: String,
    val city: String,
    val stateProvinceCode: String,
    val postalCode: String
)

data class CommunicationInfo(
    val telephone: String
)

data class NationalId(
    val number: String,
    val type: String,
)