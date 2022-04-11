package com.belcobtm.data.rest.settings.response

import com.belcobtm.data.rest.settings.request.VerificationDocumentRequest

data class VerificationUserDocumentResponse(
    val documentVerification: VerificationDocument
)

data class VerificationDocument(
    val request: VerificationDocumentRequest?,
    val response: VerificationDocumentResponse?,
)

data class VerificationDocumentResponse(
    val transactionID: String?,
    val countryCode: String?,
    val record: RecordDocumentResponse
)

data class RecordDocumentResponse(
    val recordStatus: String?,
    val datasourceResults: List<DataSourceDocumentResult>
)

data class DataSourceDocumentResult(
    val errors: List<DataSourceError>
)
