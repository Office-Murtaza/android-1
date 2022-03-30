package com.belcobtm.data.rest.settings.response

data class VerificationIdentityResponse(
    val countryCode: String?,
    val record: RecordIdentityResponse
)

data class RecordIdentityResponse(
    val recordStatus: String?,
)