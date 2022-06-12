package com.belcobtm.domain.bank_account.item

import android.os.Parcelable
import com.belcobtm.domain.bank_account.type.BankAccountPaymentStatusType
import com.belcobtm.domain.bank_account.type.BankAccountPaymentType
import com.belcobtm.domain.bank_account.type.BankAccountType
import com.belcobtm.presentation.core.adapter.model.ListItem
import kotlinx.parcelize.Parcelize

@Parcelize
data class BankAccountPaymentListItem(
    override val id: String,
    val step: Int,
    val bankAccountId: String,
    val paymentType: BankAccountPaymentType,
    val accountType: BankAccountType,
    val platformFee: Double?,
    val networkFee: Double?,
    val networkFeeCurrency: String?,
    val date: String,
    val timestamp: Long,
    val usdPaymentStatus: BankAccountPaymentStatusType,
    val usdcTransferStatus: BankAccountPaymentStatusType,
    val usdcTransferHash: String?,
    val usdAmount: String,
    val usdcAmount: String,
) : ListItem, Parcelable {
    override val type: Int
        get() = BANK_ACCOUNT_PAYMENT_ITEM_TYPE

    companion object {
        const val BANK_ACCOUNT_PAYMENT_ITEM_TYPE = 2
    }
}