package com.belcobtm.domain.settings.item

import android.net.Uri

data class VerificationVipDataItem(
    val firstName: String,
    val lastName: String,
    val address: String,
    val city: String,
    val country: String,
    val province: String,
    val zipCode: String,
    val idCardNumber: String,
    val idCardNumberFilename: String,
    val fileUri: Uri,
    val ssn: Int
)