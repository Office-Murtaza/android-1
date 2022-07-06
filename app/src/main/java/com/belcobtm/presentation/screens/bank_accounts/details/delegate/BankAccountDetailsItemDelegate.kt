package com.belcobtm.presentation.screens.bank_accounts.details.delegate

import android.view.LayoutInflater
import android.view.ViewGroup
import com.belcobtm.databinding.ItemBankAccountDetailsBuySellBinding
import com.belcobtm.domain.bank_account.item.BankAccountDetailsListItem
import com.belcobtm.presentation.core.adapter.delegate.AdapterDelegate

class BankAccountDetailsItemDelegate(
    private val onBankAccountDetailsClicked: (Boolean) -> Unit,
    private val onBuyClicked: () -> Unit,
    private val onSellClicked: () -> Unit,
) : AdapterDelegate<BankAccountDetailsListItem, BankAccountDetailsItemViewHolder>() {

    override val viewType: Int
        get() = BankAccountDetailsListItem.BANK_ACCOUNT_DETAILS_ITEM_TYPE

    override fun createHolder(
        parent: ViewGroup,
        inflater: LayoutInflater
    ): BankAccountDetailsItemViewHolder =
        BankAccountDetailsItemViewHolder(
            ItemBankAccountDetailsBuySellBinding.inflate(inflater, parent, false),
            onBankAccountDetailsClicked,
            onBuyClicked,
            onSellClicked,
        )
}