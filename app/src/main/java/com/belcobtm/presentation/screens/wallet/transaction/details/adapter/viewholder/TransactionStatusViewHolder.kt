package com.belcobtm.presentation.screens.wallet.transaction.details.adapter.viewholder

import com.belcobtm.databinding.ItemTransactionStatusBinding
import com.belcobtm.presentation.screens.wallet.transaction.details.adapter.TransactionDetailsAdapter

class TransactionStatusViewHolder(private val binding: ItemTransactionStatusBinding) :
    BaseTransactionViewHolder(binding.root) {

    override fun bindHodler(item: TransactionDetailsAdapter.Item) {
        val statusItem = item as TransactionDetailsAdapter.Item.Status
        binding.tvStatusKey.setText(statusItem.key)
        binding.tvStatusValue.setText(statusItem.value)
        binding.ivStatusIcon.setImageResource(statusItem.imageRes)
    }
}
