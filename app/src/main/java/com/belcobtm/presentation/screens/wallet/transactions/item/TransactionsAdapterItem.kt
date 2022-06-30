package com.belcobtm.presentation.screens.wallet.transactions.item

import androidx.recyclerview.widget.DiffUtil
import com.belcobtm.domain.transaction.item.TransactionDetailsDataItem
import com.belcobtm.domain.transaction.type.TransactionStatusType
import com.belcobtm.domain.transaction.type.TransactionType
import com.belcobtm.presentation.core.DateFormat

data class TransactionsAdapterItem(
    val id: String,
    val dbId: String,
    val date: String,
    val cryptoAmount: Double,
    val status: TransactionStatusType,
    val type: TransactionType
)

fun TransactionDetailsDataItem.mapToUiItem(): TransactionsAdapterItem =
    TransactionsAdapterItem(
        id = hash.orEmpty(),
        dbId = gbId,
        date = DateFormat.sdfShort.format(timestamp),
        cryptoAmount = cryptoAmount ?: 0.0,
        type = type,
        status = statusType
    )

class TransactionsAdapterItemCallback : DiffUtil.ItemCallback<TransactionsAdapterItem>() {
    override fun areItemsTheSame(
        oldItem: TransactionsAdapterItem,
        newItem: TransactionsAdapterItem
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: TransactionsAdapterItem,
        newItem: TransactionsAdapterItem
    ): Boolean {
        return oldItem == newItem
    }
}