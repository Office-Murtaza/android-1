package com.belcobtm.presentation.screens.wallet.transactions.item

import com.belcobtm.domain.transaction.item.TransactionDomainModel
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

fun TransactionDomainModel.mapToUiItem(): TransactionsAdapterItem =
    TransactionsAdapterItem(
        id = hash ?: gbId,
        dbId = gbId,
        date = formatDate(timestamp),
        cryptoAmount = cryptoAmount ?: 0.0,
        type = type,
        status = statusType
    )

@Synchronized
fun formatDate(timestamp: Long): String = DateFormat.sdfShort.format(timestamp)
