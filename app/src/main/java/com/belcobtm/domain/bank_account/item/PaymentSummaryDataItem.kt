package com.belcobtm.domain.bank_account.item

import android.os.Parcelable
import com.belcobtm.domain.bank_account.type.BankAccountPaymentType
import com.belcobtm.domain.bank_account.type.BankAccountType
import kotlinx.parcelize.Parcelize

@Parcelize
data class PaymentSummaryDataItem(
    val bankAccountId:String,
    val paymentType: BankAccountPaymentType,
    val accountId: String,
    val bankAccountType: BankAccountType,
    val valueExchangeFrom: Double,
    val valueExchangeTo: Double,
    val networkFee: Double,
    val platformFeePercent: Int,
    val platformFeeValue: Double,
    val processingTime: String,
    val trackingRef: String?,
    val walletId: String,
    val walletAddress: String,
    val instructionsDataItem: PaymentInstructionsDataItem?
) : Parcelable


