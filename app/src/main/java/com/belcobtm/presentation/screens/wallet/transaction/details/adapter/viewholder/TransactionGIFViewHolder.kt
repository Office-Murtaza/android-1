package com.belcobtm.presentation.screens.wallet.transaction.details.adapter.viewholder

import com.belcobtm.databinding.ItemTransactionGifBinding
import com.belcobtm.presentation.screens.wallet.transaction.details.adapter.TransactionDetailsAdapter

class TransactionGIFViewHolder(private val binding: ItemTransactionGifBinding) :
    BaseTransactionViewHolder(binding.root) {

    override fun bindHodler(item: TransactionDetailsAdapter.Item) {
        val gifItem = item as TransactionDetailsAdapter.Item.GIF
        binding.mediaView.setMediaWithId(gifItem.gifId)
        binding.tvGifMessage.text = gifItem.message
    }
}
