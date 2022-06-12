package com.belcobtm.domain.bank_account.item

data class BankAccountLinkDataItem(
    val publicToken: String,
    val accountsId: List<String>,
    val bankName: String,
)