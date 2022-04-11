package com.belcobtm.domain.settings.item

import android.graphics.Bitmap
import com.belcobtm.domain.settings.type.DocumentType
import com.belcobtm.domain.settings.type.RecordStatus

data class VerificationDocumentResponseDataItem(
    val recordStatus: RecordStatus,
    val documentType: DocumentType,
    val transactionId: String?,
    val frontImageBitmap: Bitmap?,
    val backImageBitmap: Bitmap?,
    val selfieImageBitmap: Bitmap?,
    val frontImageValidationError: Boolean,
    val backImageValidationError: Boolean,
    val selfieImageValidationError: Boolean
)