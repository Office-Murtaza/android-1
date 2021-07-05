package com.belcobtm.domain.settings.item

import android.net.Uri

data class VerificationVipDataItem(
    val fileUri: Uri,
    val ssn: Int
)