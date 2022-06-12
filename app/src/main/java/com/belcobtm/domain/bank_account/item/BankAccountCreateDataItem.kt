package com.belcobtm.domain.bank_account.item

data class BankAccountCreateDataItem(
    val accountNumber: String? = null,
    val routingNumber: String? = null,
    val iban: String? = null,
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val country: String? = null,
    val province: String? = null,
    val city: String? = null,
    val address: String? = null,
    val zipCode: String? = null,
    val bankName: String? = null,
    val bankCountry: String? = null,
    val bankCity: String? = null,
)