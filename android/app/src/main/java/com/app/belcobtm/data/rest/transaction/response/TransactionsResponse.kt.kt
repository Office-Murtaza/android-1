package com.app.belcobtm.data.rest.transaction.response

import com.app.belcobtm.domain.transaction.item.TransactionDataItem
import com.app.belcobtm.domain.transaction.type.TransactionStatusType
import com.app.belcobtm.domain.transaction.type.TransactionType

data class GetTransactionsResponse(
    val total: Int,
    val transactions: List<TransactionItemResponse>
)

data class TransactionItemResponse(
    val txId: String?,
    val txDbId: String?,
    val index: Int,
    val date1: String,
    val status: Int,
    val type: Int,
    val cryptoAmount: Double
)

fun TransactionItemResponse.mapToDataItem(): TransactionDataItem = TransactionDataItem(
    txId = txId ?: "",
    txDbId = txDbId ?: "",
    index = index,
    date = date1,
    cryptoAmount = cryptoAmount,
    type = TransactionType.values().firstOrNull { it.code == type } ?: TransactionType.UNKNOWN,
    status = TransactionStatusType.values().firstOrNull { it.code == status } ?: TransactionStatusType.UNKNOWN
)