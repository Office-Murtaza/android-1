package com.app.belcobtm.presentation.features.wallet.transactions.item

import androidx.recyclerview.widget.DiffUtil
import com.app.belcobtm.domain.transaction.item.TransactionDataItem
import com.app.belcobtm.domain.transaction.type.TransactionStatusType
import com.app.belcobtm.domain.transaction.type.TransactionType
import com.app.belcobtm.presentation.core.DateFormat
import java.text.SimpleDateFormat

data class TransactionsAdapterItem(
    val id: String,
    val dbId: String,
    val date: String,
    val cryptoAmount: Double,
    val status: TransactionStatusType,
    val type: TransactionType
)

fun TransactionDataItem.mapToUiItem(): TransactionsAdapterItem =
    TransactionsAdapterItem(
        id = txId,
        dbId = txDbId,
        date = DateFormat.sdfShort.format(timestamp),
        cryptoAmount = cryptoAmount,
        type = type,
        status = status
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