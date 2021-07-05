package com.belcobtm.domain.settings.item

data class VerificationStateDataItem(
    val code: String,
    val name: String,
    val cities: List<String>
)