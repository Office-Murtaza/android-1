package com.belcobtm.domain.settings.item

data class VerificationDetailsDataItem(
    val selectedCountry:VerificationSupportedCountryDataItem?,
    val identityVerification: VerificationIdentityResponseDataItem?,
    val documentVerification: VerificationDocumentResponseDataItem?,
    val supportedCountries: List<VerificationSupportedCountryDataItem>,
    val sdkToken: String?,
)