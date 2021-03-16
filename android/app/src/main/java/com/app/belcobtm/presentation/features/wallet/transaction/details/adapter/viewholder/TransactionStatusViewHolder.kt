package com.app.belcobtm.presentation.features.wallet.transaction.details.adapter.viewholder

import com.app.belcobtm.databinding.ItemTransactionStatusBinding
import com.app.belcobtm.presentation.features.wallet.transaction.details.adapter.TransactionDetailsAdapter

class TransactionStatusViewHolder(private val binding: ItemTransactionStatusBinding) :
    BaseTransactionViewHolder(binding.root) {

    override fun bindHodler(item: TransactionDetailsAdapter.Item) {
        val statusItem = item as TransactionDetailsAdapter.Item.Status
        binding.tvStatusKey.setText(statusItem.key)
        binding.tvStatusValue.setText(statusItem.value)
        binding.ivStatusIcon.setImageResource(statusItem.imageRes)
    }
}
