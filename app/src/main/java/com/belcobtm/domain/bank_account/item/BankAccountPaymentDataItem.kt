package com.belcobtm.domain.bank_account.item

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class BankAccountPaymentDataItem(
    val walletId: String,
    val walletAddress: String,
    val feePercent: Int,
    val payments: List<BankAccountPaymentListItem>,
    val limit: BankAccountLimitDataItem
)

@Parcelize
data class BankAccountLimitDataItem(
    val achLimit: Int?,
    val wireLimit: Int?
) : Parcelable

@Parcelize
data class BankAccountInfoDataItem(
    val walletId: String,
    val walletAddress: String,
    val feePercent: Int,
    val limit: BankAccountLimitDataItem

) : Parcelable