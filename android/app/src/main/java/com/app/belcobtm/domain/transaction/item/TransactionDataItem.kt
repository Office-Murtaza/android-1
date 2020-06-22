package com.app.belcobtm.domain.transaction.item

import com.app.belcobtm.domain.transaction.type.TransactionStatusType
import com.app.belcobtm.domain.transaction.type.TransactionType

data class TransactionDataItem(
    val date: String,
    val index: Int,
    val txId: String,
    val txDbId: String,
    val cryptoAmount: Double,
    val type: TransactionType,
    val status: TransactionStatusType
)