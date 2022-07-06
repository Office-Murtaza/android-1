package com.belcobtm.presentation.screens.bank_accounts.delegate

import androidx.core.content.ContextCompat
import com.belcobtm.databinding.ItemBankAccountBinding
import com.belcobtm.domain.bank_account.item.BankAccountListItem
import com.belcobtm.domain.bank_account.type.BankAccountType
import com.belcobtm.presentation.core.adapter.holder.MultiTypeViewHolder
import com.belcobtm.presentation.tools.extensions.formatBalanceValue
import com.belcobtm.presentation.tools.extensions.toggle

class BankAccountItemViewHolder(
    private val binding: ItemBankAccountBinding,
    onBankAccountClicked: (BankAccountListItem) -> Unit,
) : MultiTypeViewHolder<BankAccountListItem>(binding.root) {

    init {
        binding.root.setOnClickListener {
            onBankAccountClicked(model)
        }
    }

    override fun bind(model: BankAccountListItem) {
        with(binding) {
            bankNameView.text = model.bankName
            accountNameView.text = model.accountName
            balanceValueView.text =
                model.balanceValue?.formatBalanceValue(model.balanceCurrency) ?: ""
            statusView.text = model.status.stringValue
            statusView.setCompoundDrawablesWithIntrinsicBounds(
                null, null,
                ContextCompat.getDrawable(binding.root.context, model.status.iconResource), null
            )
            achTypeChip.toggle(model.bankAccountTypes.contains(BankAccountType.ACH))
            wireTypeChip.toggle(model.bankAccountTypes.contains(BankAccountType.WIRE))
        }
    }
}