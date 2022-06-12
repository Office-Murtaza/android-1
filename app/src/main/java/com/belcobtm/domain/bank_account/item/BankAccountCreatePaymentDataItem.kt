package com.belcobtm.domain.bank_account.item

import com.belcobtm.domain.bank_account.type.BankAccountPaymentType
import com.belcobtm.domain.bank_account.type.BankAccountType

data class BankAccountCreatePaymentDataItem(
    val bankAccountId: String,
    val type: BankAccountPaymentType,
    val transferHex: String?,
    val transferSourceId: String?,
    val transferSourceAddress: String?,
    val transferDestinationAddress: String?,
    val transferAmount: Double,
    val paymentDestinationType: BankAccountType?,
    val paymentDestinationId: String?,
    val paymentAmount: Double,
    val paymentBeneficiaryEmail: String?,
    val paymentEmail: String?,
    val paymentSourceId: String?,
    val paymentSourceType: BankAccountType?,
    val cryptoFee: Double,
    val cryptoFeeCurrency: String,
    val paymentInstructions:PaymentInstructionsDataItem?,

    )