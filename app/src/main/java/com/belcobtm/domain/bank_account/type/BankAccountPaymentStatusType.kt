package com.belcobtm.domain.bank_account.type

import com.belcobtm.R

enum class BankAccountPaymentStatusType(val stringValue: String, val iconResource: Int) {
    PENDING("Pending", R.drawable.ic_pending_status),
    COMPLETE("Complete", R.drawable.ic_verification_complete),
    WAIT("Wait", R.drawable.ic_wait),
    FAIL("Fail", R.drawable.ic_fail);

    companion object {
        fun fromString(string: String?): BankAccountPaymentStatusType =
            BankAccountPaymentStatusType.values()
                .find { it.stringValue.lowercase() == string?.lowercase() } ?: WAIT
    }
}