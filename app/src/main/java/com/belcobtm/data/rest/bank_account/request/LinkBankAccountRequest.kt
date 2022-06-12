package com.belcobtm.data.rest.bank_account.request

data class LinkBankAccountRequest(
    val publicToken: String,
    val accountIds: List<String>,
    val bankName: String,
)