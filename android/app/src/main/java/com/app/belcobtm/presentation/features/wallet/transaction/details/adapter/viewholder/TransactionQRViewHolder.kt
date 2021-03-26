package com.app.belcobtm.presentation.features.wallet.transaction.details.adapter.viewholder

import com.app.belcobtm.databinding.ItemTransactionQrBinding
import com.app.belcobtm.presentation.features.wallet.transaction.details.adapter.TransactionDetailsAdapter

class TransactionQRViewHolder(private val binding: ItemTransactionQrBinding) :
    BaseTransactionViewHolder(binding.root) {

    override fun bindHodler(item: TransactionDetailsAdapter.Item) {
        val qrItem = item as TransactionDetailsAdapter.Item.QR
        binding.ivQR.setImageBitmap(qrItem.bitmap)
    }
}
