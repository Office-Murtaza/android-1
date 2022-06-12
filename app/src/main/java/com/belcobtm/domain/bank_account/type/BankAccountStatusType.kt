package com.belcobtm.domain.bank_account.type

import com.belcobtm.R

enum class BankAccountStatusType(val stringValue: String, val iconResource: Int) {
    PENDING("PENDING", R.drawable.ic_pending_status),
    COMPLETE("COMPLETE", R.drawable.ic_verification_complete),
    UNLINKED("UNLINKED", R.drawable.ic_unlinked),
    FAIL("FAILED", R.drawable.ic_fail),
    NONE("none", R.drawable.ic_pending_status);

    companion object {
        fun fromString(string: String?): BankAccountStatusType =
            values().find { it.stringValue.lowercase() == string?.lowercase() } ?: NONE
    }
}