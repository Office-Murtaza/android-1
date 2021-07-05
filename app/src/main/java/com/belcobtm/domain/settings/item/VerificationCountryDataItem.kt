package com.belcobtm.domain.settings.item

data class VerificationCountryDataItem(
    val code: String,
    val name: String,
    val states: List<VerificationStateDataItem>
)