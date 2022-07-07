package com.belcobtm.domain.transaction

import com.belcobtm.domain.transaction.item.TransactionDomainModel

data class TransactionsCacheModel(
    val transactionsMap: Map<String, TransactionDomainModel> = emptyMap(), // HashMap for all except TRX
    val trxTransactions: List<TransactionDomainModel> = emptyList()
)