package com.belcobtm.domain.settings.type

enum class VerificationDocumentFieldsType(val stringValue: String) {
    FRONT_SCAN("frontScan"),
    BACK_SCAN("backScan"),
    SELFIE("selfie"),
    NONE("none");

    companion object {
        fun fromString(string: String?): VerificationDocumentFieldsType =
            values().find { it.stringValue.compareTo(string ?: "none", ignoreCase = true) == 0 }
                ?: NONE
    }
}