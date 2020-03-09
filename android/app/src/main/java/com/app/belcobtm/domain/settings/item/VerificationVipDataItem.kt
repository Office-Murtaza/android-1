package com.app.belcobtm.domain.settings.item

import android.net.Uri

data class VerificationVipDataItem(
    val tierId: Int,
    val fileUri: Uri,
    val ssn: String
)