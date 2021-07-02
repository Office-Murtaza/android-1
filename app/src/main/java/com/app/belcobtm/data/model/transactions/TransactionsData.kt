package com.app.belcobtm.data.model.transactions

import com.app.belcobtm.domain.transaction.item.TransactionDetailsDataItem

data class TransactionsData(
    val transactions: Map<String, TransactionDetailsDataItem>
)