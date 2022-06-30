package com.belcobtm.presentation.features.bank_accounts.details.delegate

import android.view.LayoutInflater
import android.view.ViewGroup
import com.belcobtm.databinding.ItemBankAccountNoPaymentBinding
import com.belcobtm.domain.bank_account.item.BankAccountNoPaymentsListItem
import com.belcobtm.presentation.core.adapter.delegate.AdapterDelegate
import com.belcobtm.presentation.core.adapter.model.ListItem
import com.belcobtm.presentation.features.bank_accounts.details.BankAccountDetailsFragment

class BankAccountNoPaymentItemDelegate : AdapterDelegate<BankAccountNoPaymentsListItem, BankAccountNoPaymentItemViewHolder>() {

    override val viewType: Int
        get() = BankAccountNoPaymentsListItem.BANK_ACCOUNT_NO_PAYMENTS_ITEM_TYPE

    override fun createHolder(
        parent: ViewGroup,
        inflater: LayoutInflater
    ): BankAccountNoPaymentItemViewHolder =
        BankAccountNoPaymentItemViewHolder(
            ItemBankAccountNoPaymentBinding.inflate(inflater, parent, false),
        )
}