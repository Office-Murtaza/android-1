package com.belcobtm.data.rest.settings.request

data class VerificationDocumentRequest(
    val countryCode: String,
    val documentType: String,
    val base64: Base64VerificationDocuments,
    val firebase: FirebaseVerificationDocuments

)

data class Base64VerificationDocuments(
    val frontImage: String,
    val backImage: String,
    val selfieImage: String,
)

data class FirebaseVerificationDocuments(
    val frontImage: String,
    val backImage: String,
    val selfieImage: String,
)
