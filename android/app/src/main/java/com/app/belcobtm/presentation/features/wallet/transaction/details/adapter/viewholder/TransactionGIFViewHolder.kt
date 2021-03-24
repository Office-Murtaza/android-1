package com.app.belcobtm.presentation.features.wallet.transaction.details.adapter.viewholder

import com.app.belcobtm.databinding.ItemTransactionGifBinding
import com.app.belcobtm.presentation.features.wallet.transaction.details.adapter.TransactionDetailsAdapter
import com.giphy.sdk.ui.views.GPHMediaView

class TransactionGIFViewHolder(private val binding: ItemTransactionGifBinding) :
    BaseTransactionViewHolder(binding.root) {

    override fun bindHodler(item: TransactionDetailsAdapter.Item) {
        val gifItem = item as TransactionDetailsAdapter.Item.GIF
        val mediaView = GPHMediaView(binding.root.context)
        mediaView.setMediaWithId(gifItem.gifId)
        binding.ivGif.setImageDrawable(mediaView.drawable)
        binding.tvGifMessage.text = gifItem.message
    }
}
