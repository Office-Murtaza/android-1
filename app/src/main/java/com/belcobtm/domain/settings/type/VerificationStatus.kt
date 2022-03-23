package com.belcobtm.domain.settings.type

enum class VerificationStatus(val stringValue: String) {
    VERIFIED("VERIFIED"),
    UNVERIFIED("UNVERIFIED"),
    NOT_VERIFIED("NOT_VERIFIED"),
    VERIFICATION_PENDING("VERIFICATION_PENDING"),
    VERIFICATION_REJECTED("VERIFICATION_REJECTED"),
    VIP_VERIFICATION_PENDING("VIP_VERIFICATION_PENDING"),
    VIP_VERIFICATION_REJECTED("VIP_VERIFICATION_REJECTED"),
    VIP_VERIFIED("VIP_VERIFIED");

    companion object {
        fun fromString(string: String?): VerificationStatus =
            values().find { it.stringValue == string } ?: UNVERIFIED
    }
}

fun VerificationStatus.isVerified(): Boolean {
    return stringValue == VerificationStatus.VERIFIED.stringValue
}

fun VerificationStatus.isPending(): Boolean {
    return stringValue == VerificationStatus.VERIFIED.stringValue
}