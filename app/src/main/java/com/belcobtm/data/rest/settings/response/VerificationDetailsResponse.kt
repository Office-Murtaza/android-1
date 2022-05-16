package com.belcobtm.data.rest.settings.response

import com.belcobtm.domain.settings.item.VerificationSupportedCountryDataItem

data class VerificationDetailsResponse(
    val userVerification: UserVerificationResponse?,
    val supportedCountries: List<VerificationSupportedCountryDataItem>,
    val sdkToken: String?,
)

data class UserVerificationResponse(
    val countryCode: String?,
    val sourceOfFunds: String?,
    val occupation: String?,
    val identityVerification: VerificationIdentity?,
    val documentVerification: VerificationDocument?,
)

