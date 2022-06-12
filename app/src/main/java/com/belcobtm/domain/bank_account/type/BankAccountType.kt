package com.belcobtm.domain.bank_account.type

enum class BankAccountType(val stringValue: String) {
    ACH("ACH"),
    WIRE("WIRE"),
    NONE("none");

    companion object {
        fun fromString(string: String?): BankAccountType =
            values().find { it.stringValue.lowercase() == string?.lowercase() } ?: NONE
    }
}