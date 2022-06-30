package com.belcobtm.domain.settings.type

enum class VerificationStatus(val stringValue: String) {

    VERIFIED("VERIFIED"),
    UNVERIFIED("UNVERIFIED"),
    PENDING("PENDING");

    companion object {

        fun fromString(string: String?): VerificationStatus =
            values().find { it.stringValue == string } ?: UNVERIFIED
    }
}

fun VerificationStatus.isVerified(): Boolean {
    return stringValue == VerificationStatus.VERIFIED.stringValue
}

fun VerificationStatus.isPending(): Boolean {
    return stringValue == VerificationStatus.PENDING.stringValue
}
