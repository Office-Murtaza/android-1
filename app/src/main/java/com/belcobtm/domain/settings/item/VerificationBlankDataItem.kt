package com.belcobtm.domain.settings.item

import android.net.Uri

data class VerificationBlankDataItem(
    val id: String?,
    val imageUri: Uri,
    val idNumber: String,
    val firstName: String,
    val lastName: String,
    val address: String,
    val city: String,
    val country: String,
    val province: String,
    val zipCode: String,
)