package com.belcobtm.domain.bank_account.item

import com.belcobtm.presentation.core.adapter.model.ListItem

data class BankAccountNoPaymentsListItem(
    override val id: String,
) : ListItem {

    override val type: Int
        get() = BANK_ACCOUNT_NO_PAYMENTS_ITEM_TYPE

    companion object{
        const val BANK_ACCOUNT_NO_PAYMENTS_ITEM_TYPE = 3
    }

}