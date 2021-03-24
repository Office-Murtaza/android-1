package com.app.belcobtm.presentation.features.wallet.transaction.details.adapter.viewholder

import com.app.belcobtm.databinding.ItemTransactionIdBinding
import com.app.belcobtm.presentation.features.wallet.transaction.details.adapter.TransactionDetailsAdapter

class TransactionIdViewHolder(
    private val binding: ItemTransactionIdBinding,
    private val clickListener: TransactionDetailsAdapter.IOnLinkClickListener
) : BaseTransactionViewHolder(binding.root) {

    override fun bindHodler(item: TransactionDetailsAdapter.Item) {
        val idItem = item as TransactionDetailsAdapter.Item.Id
        binding.tvIdKey.setText(idItem.key)
        binding.tvIdValue.text = idItem.value
        binding.root.setOnClickListener { clickListener.onLinkClicked(idItem.link) }
    }
}
