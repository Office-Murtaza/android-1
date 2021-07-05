package com.belcobtm.domain.settings.type

enum class VerificationStatus(val code: Int) {
    NOT_VERIFIED(1),
    VERIFICATION_PENDING(2),
    VERIFICATION_REJECTED(3),
    VERIFIED(4),
    VIP_VERIFICATION_PENDING(5),
    VIP_VERIFICATION_REJECTED(6),
    VIP_VERIFIED(7);

    companion object {
        fun getStatusByCode(code: Int): VerificationStatus = values().find { it.code == code } ?: NOT_VERIFIED
    }
}