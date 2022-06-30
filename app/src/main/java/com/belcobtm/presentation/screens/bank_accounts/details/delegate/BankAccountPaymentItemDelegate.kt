package com.belcobtm.presentation.screens.bank_accounts.details.delegate

import android.view.LayoutInflater
import android.view.ViewGroup
import com.belcobtm.databinding.ItemBankAccountTransactionBinding
import com.belcobtm.domain.bank_account.item.BankAccountPaymentListItem
import com.belcobtm.presentation.core.adapter.delegate.AdapterDelegate

class BankAccountPaymentItemDelegate(
    private val onBankAccountPaymentClicked: (BankAccountPaymentListItem) -> Unit
) : AdapterDelegate<BankAccountPaymentListItem, BankAccountPaymentItemViewHolder>() {

    override val viewType: Int
        get() = BankAccountPaymentListItem.BANK_ACCOUNT_PAYMENT_ITEM_TYPE

    override fun createHolder(
        parent: ViewGroup,
        inflater: LayoutInflater
    ): BankAccountPaymentItemViewHolder =
        BankAccountPaymentItemViewHolder(
            ItemBankAccountTransactionBinding.inflate(inflater, parent, false),
            onBankAccountPaymentClicked
        )
}