package com.belcobtm.data.model.transactions

import com.belcobtm.domain.transaction.item.TransactionDetailsDataItem

data class TransactionsData(
    val transactions: Map<String, TransactionDetailsDataItem>
)