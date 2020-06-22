package com.app.belcobtm.presentation.features.wallet.transactions.item

import com.app.belcobtm.domain.transaction.item.TransactionDataItem
import com.app.belcobtm.domain.transaction.type.TransactionStatusType
import com.app.belcobtm.domain.transaction.type.TransactionType

data class TransactionsAdapterItem(
    val id: String,
    val dbId: String,
    val date: String,
    val cryptoAmount: Double,
    val status: TransactionStatusType,
    val type: TransactionType
)

fun TransactionDataItem.mapToUiItem(): TransactionsAdapterItem = TransactionsAdapterItem(
    id = txId,
    dbId = txDbId,
    date = date,
    cryptoAmount = cryptoAmount,
    type = type,
    status = status
)