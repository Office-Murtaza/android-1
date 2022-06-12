package com.belcobtm.domain.bank_account.type

enum class BankAccountPaymentType(val stringValue: String) {
    BUY("BUY"),
    SELL("SELL"),
    NONE("NONE");

    companion object {
        fun fromString(string: String?): BankAccountPaymentType =
            BankAccountPaymentType.values().find { it.stringValue == string } ?: NONE
    }
}