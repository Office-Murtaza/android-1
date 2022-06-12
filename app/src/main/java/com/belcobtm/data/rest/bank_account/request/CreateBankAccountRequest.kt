package com.belcobtm.data.rest.bank_account.request

import com.belcobtm.data.rest.bank_account.response.AccountDetails
import com.belcobtm.data.rest.bank_account.response.BankAddress
import com.belcobtm.data.rest.bank_account.response.BillingDetails

data class CreateBankAccountRequest(
    val bankAddress: BankAddress?,
    val billingDetails: BillingDetails?,
    val accountDetails: AccountDetails?,
    val bankName: String?,
)