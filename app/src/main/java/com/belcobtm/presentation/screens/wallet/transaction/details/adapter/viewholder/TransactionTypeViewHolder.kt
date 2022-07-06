package com.belcobtm.presentation.screens.wallet.transaction.details.adapter.viewholder

import com.belcobtm.databinding.ItemTransactionTypeBinding
import com.belcobtm.presentation.screens.wallet.transaction.details.adapter.TransactionDetailsAdapter

class TransactionTypeViewHolder(private val binding: ItemTransactionTypeBinding) :
    BaseTransactionViewHolder(binding.root) {

    override fun bindHodler(item: TransactionDetailsAdapter.Item) {
        val transactionTypeItem = item as TransactionDetailsAdapter.Item.Type
        binding.transactionTypeView.setTransactionType(transactionTypeItem.type)
    }
}
