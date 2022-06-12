package com.belcobtm.data.rest.bank_account.request

import com.belcobtm.data.rest.bank_account.response.Payment
import com.belcobtm.data.rest.bank_account.response.PaymentTransfer

data class CreateBankAccountPaymentRequest(
    val type: String,
    val bankAccountId: String,
    val accountType: String?,
    val cryptoFee: Double,
    val cryptoFeeCurrency: String,
    val transfer: PaymentTransfer,
    val payment: Payment,
    val latitude: Double?,
    val longitude: Double?,
)