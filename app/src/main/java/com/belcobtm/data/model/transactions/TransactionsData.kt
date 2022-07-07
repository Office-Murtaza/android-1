package com.belcobtm.data.model.transactions

import com.belcobtm.domain.transaction.item.TransactionDomainModel

data class TransactionsData(
    val transactions: Map<String, TransactionDomainModel>
)
