package com.belcobtm.presentation.screens.wallet.transaction.details.adapter.viewholder

import com.belcobtm.databinding.ItemTransactionQrBinding
import com.belcobtm.presentation.screens.wallet.transaction.details.adapter.TransactionDetailsAdapter

class TransactionQRViewHolder(private val binding: ItemTransactionQrBinding) :
    BaseTransactionViewHolder(binding.root) {

    override fun bindHodler(item: TransactionDetailsAdapter.Item) {
        val qrItem = item as TransactionDetailsAdapter.Item.QR
        binding.ivQR.setImageBitmap(qrItem.bitmap)
    }
}
