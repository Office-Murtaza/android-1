package com.belcobtm.data.rest.settings.response

import com.belcobtm.domain.settings.item.VerificationIdentityDataItem
import com.belcobtm.domain.settings.item.VerificationSupportedCountryDataItem

data class VerificationDetailsResponse(
    val identityVerification: VerificationIdentityDataItem?,
    val documentVerification: Any?,
    val documentVerificationComplete: Boolean?,
    val supportedCountries: List<VerificationSupportedCountryDataItem>,
    val sdkToken: String?,
)

