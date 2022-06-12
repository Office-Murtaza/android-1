package com.belcobtm.data.model.bank_account

import com.belcobtm.domain.bank_account.item.BankAccountDataItem

data class BankAccountsData(
    val bankAccounts: Map<String, BankAccountDataItem>
)
