package com.belcobtm.domain.bank_account.item

import android.os.Parcelable
import com.belcobtm.domain.bank_account.type.BankAccountStatusType
import com.belcobtm.domain.bank_account.type.BankAccountType
import com.belcobtm.presentation.core.adapter.model.ListItem
import kotlinx.parcelize.Parcelize

@Parcelize
data class BankAccountListItem(
    override val id: String,
    val bankName: String,
    val accountName: String,
    val balanceValue: Double?,
    val balanceCurrency: String?,
    val status: BankAccountStatusType,
    val bankAccountTypes: List<BankAccountType>,
    val billingDetails: BillingDetailsDataItem,
    val plaidDetails: PlaidDetailsDataItem,
    val accountDetails: AccountDetailsDataItem,
    val circleDetails: CircleDetailsDataItem,
    val date: String,
    val createdAt: Long,
) : ListItem, Parcelable {

    override val type: Int
        get() = BANK_ACCOUNT_ITEM_TYPE

    companion object {
        const val BANK_ACCOUNT_ITEM_TYPE = 1
    }
}