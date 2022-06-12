package com.belcobtm.domain.bank_account.item

import com.belcobtm.domain.bank_account.type.BankAccountStatusType
import com.belcobtm.domain.bank_account.type.BankAccountType
import com.belcobtm.presentation.core.adapter.model.ListItem

data class BankAccountDetailsListItem(
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
    var isExpanded: Boolean,
) : ListItem {

    override val type: Int
        get() = BANK_ACCOUNT_DETAILS_ITEM_TYPE

    companion object {
        const val BANK_ACCOUNT_DETAILS_ITEM_TYPE = 1
    }

    fun isPlaidDetailsEmpty(): Boolean {
        with(plaidDetails)
        {
            if (accountId.isEmpty() && type.isEmpty() && subtype.isEmpty() && achNumber.isEmpty() && routingNumber.isEmpty() && wireRouting.isEmpty())
                return true
            return false
        }
    }

    fun isAccountDetailsEmpty(): Boolean {
        with(accountDetails)
        {
            if (accountNumber.isEmpty() && routingNumber.isEmpty() && iban.isEmpty())
                return true
            return false
        }
    }

    fun isCircleDetailsEmpty(): Boolean {
        with(circleDetails)
        {
            if (achAccountId.isEmpty() && wireAccountId.isEmpty())
                return true
            return false
        }
    }

    fun isBillingDetailsEmpty(): Boolean {
        with(billingDetails)
        {
            if (name.isEmpty() && email.isEmpty() && phone.isEmpty() && country.isEmpty() && region.isEmpty() && city.isEmpty() && street.isEmpty() && postalCode.isEmpty())
                return true
            return false
        }
    }
}