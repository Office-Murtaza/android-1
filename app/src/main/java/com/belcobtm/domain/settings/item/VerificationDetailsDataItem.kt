package com.belcobtm.domain.settings.item

data class VerificationDetailsDataItem(
    val identityVerification: VerificationIdentityResponseDataItem?,
    val documentVerification: Any?,
    val documentVerificationComplete: Boolean?,
    val supportedCountries: List<VerificationSupportedCountryDataItem>,
    val sdkToken: String?,
)