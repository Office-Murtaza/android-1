package com.belcobtm.presentation.features.wallet.transaction.details.adapter

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.belcobtm.databinding.*
import com.belcobtm.domain.transaction.type.TransactionType
import com.belcobtm.presentation.features.wallet.transaction.details.adapter.viewholder.*

class TransactionDetailsAdapter(private val linkClickListener: IOnLinkClickListener) :
    ListAdapter<TransactionDetailsAdapter.Item, BaseTransactionViewHolder>(DiffHelper()) {

    companion object {
        private const val ITEM_TYPE_REGULAR = 1
        private const val ITEM_TYPE_TRANSACTION_TYPE = 2
        private const val ITEM_TYPE_TRANSACTION_STATUS = 3
        private const val ITEM_TYPE_TRANSACTION_ID = 4
        private const val ITEM_TYPE_TRANSACTION_QR = 5
        private const val ITEM_TYPE_TRANSACTION_GIF = 6
    }

    interface IOnLinkClickListener {

        fun onLinkClicked(link: String)
    }

    /**
     * Base class for a single elemet representation on UI
     * */
    sealed class Item {
        data class Type(val type: TransactionType) : Item()
        data class Regular(@StringRes val key: Int, val value: String) : Item()
        data class Id(@StringRes val key: Int, val value: String, val link: String) : Item()
        data class QR(val bitmap: Bitmap) : Item()
        data class GIF(val gifId: String, val message: String) : Item()
        data class Status(
            @StringRes val key: Int,
            @StringRes val value: Int,
            @DrawableRes val imageRes: Int
        ) : Item()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseTransactionViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            ITEM_TYPE_REGULAR -> {
                val binding = ItemTransactionRegularBinding.inflate(inflater, parent, false)
                TransactionRegularViewHolder(binding)
            }
            ITEM_TYPE_TRANSACTION_ID -> {
                val binding = ItemTransactionIdBinding.inflate(inflater, parent, false)
                TransactionIdViewHolder(binding, linkClickListener)
            }
            ITEM_TYPE_TRANSACTION_TYPE -> {
                val binding = ItemTransactionTypeBinding.inflate(inflater, parent, false)
                TransactionTypeViewHolder(binding)
            }
            ITEM_TYPE_TRANSACTION_STATUS -> {
                val binding = ItemTransactionStatusBinding.inflate(inflater, parent, false)
                TransactionStatusViewHolder(binding)
            }
            ITEM_TYPE_TRANSACTION_QR -> {
                val binding = ItemTransactionQrBinding.inflate(inflater, parent, false)
                TransactionQRViewHolder(binding)
            }
            ITEM_TYPE_TRANSACTION_GIF -> {
                val binding = ItemTransactionGifBinding.inflate(inflater, parent, false)
                TransactionGIFViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Unsupported type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: BaseTransactionViewHolder, position: Int) {
        holder.bindHodler(getItem(position))
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is Item.Regular -> ITEM_TYPE_REGULAR
        is Item.Type -> ITEM_TYPE_TRANSACTION_TYPE
        is Item.Status -> ITEM_TYPE_TRANSACTION_STATUS
        is Item.Id -> ITEM_TYPE_TRANSACTION_ID
        is Item.QR -> ITEM_TYPE_TRANSACTION_QR
        is Item.GIF -> ITEM_TYPE_TRANSACTION_GIF
    }

    private class DiffHelper : DiffUtil.ItemCallback<Item>() {
        override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
            return oldItem::class == newItem::class
        }

        override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
            return oldItem == newItem
        }
    }
}
