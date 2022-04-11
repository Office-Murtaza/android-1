package com.belcobtm.domain.settings.item

import android.graphics.Bitmap
import com.belcobtm.domain.settings.type.DocumentType

data class VerificationDocumentDataItem(
    val frontScanBitmap: Bitmap,
    val backScanBitmap: Bitmap?,
    val selfieBitmap: Bitmap,
    val frontScanBase64: String,
    val backScanBase64: String?,
    val selfieBase64: String,
    val documentType: DocumentType,
    val countryDataItem: VerificationSupportedCountryDataItem
)