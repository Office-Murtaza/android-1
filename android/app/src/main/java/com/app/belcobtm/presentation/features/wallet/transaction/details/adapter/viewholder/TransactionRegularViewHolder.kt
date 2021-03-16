package com.app.belcobtm.presentation.features.wallet.transaction.details.adapter.viewholder

import com.app.belcobtm.databinding.ItemTransactionRegularBinding
import com.app.belcobtm.presentation.features.wallet.transaction.details.adapter.TransactionDetailsAdapter

class TransactionRegularViewHolder(
    private val binding: ItemTransactionRegularBinding
) : BaseTransactionViewHolder(binding.root) {

    override fun bindHodler(item: TransactionDetailsAdapter.Item) {
        val regularItem = item as TransactionDetailsAdapter.Item.Regular
        binding.tvRegularKey.setText(regularItem.key)
        binding.tvRegularValue.text = regularItem.value
    }
}
