package com.belcobtm.presentation.features.bank_accounts.details.delegate

import com.belcobtm.databinding.ItemBankAccountNoPaymentBinding
import com.belcobtm.domain.bank_account.item.BankAccountNoPaymentsListItem
import com.belcobtm.presentation.core.adapter.holder.MultiTypeViewHolder

class BankAccountNoPaymentItemViewHolder(
    binding: ItemBankAccountNoPaymentBinding,
) : MultiTypeViewHolder<BankAccountNoPaymentsListItem>(binding.root) {
    override fun bind(model: BankAccountNoPaymentsListItem) {
    }
}