package com.belcobtm.domain.bank_account.type

enum class CreateBankAccountType(val stringValue: String) {
    US("u.s"),
    NON_US_IBAN("non_us_iban"),
    NON_US_NON_IBAN("non_us_iban"),
}
