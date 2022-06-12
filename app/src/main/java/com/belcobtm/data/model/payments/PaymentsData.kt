package com.belcobtm.data.model.payments

import com.belcobtm.data.rest.bank_account.response.PaymentResponseData
import com.belcobtm.domain.bank_account.item.BankAccountDataItem
import com.belcobtm.domain.bank_account.item.BankAccountPaymentListItem

data class PaymentsData(
    val payments: Map<String, BankAccountPaymentListItem>
)