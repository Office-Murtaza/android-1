package com.app.belcobtm.presentation.features.wallet.transaction.details.adapter.viewholder

import com.app.belcobtm.databinding.ItemTransactionGifBinding
import com.app.belcobtm.presentation.features.wallet.transaction.details.adapter.TransactionDetailsAdapter

class TransactionGIFViewHolder(private val binding: ItemTransactionGifBinding) :
    BaseTransactionViewHolder(binding.root) {

    override fun bindHodler(item: TransactionDetailsAdapter.Item) {
        val gifItem = item as TransactionDetailsAdapter.Item.GIF
        binding.mediaView.setMediaWithId(gifItem.gifId)
        binding.tvGifMessage.text = gifItem.message
    }
}
