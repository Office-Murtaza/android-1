package com.belcobtm.data.rest.transaction.request

import com.belcobtm.domain.transaction.type.TransactionType

data class TradeRecallRequest(
    val type: String = TransactionType.RECALL.toString(),
    val cryptoAmount: Double,
    val price: Double,
    val latitude: Double?,
    val longitude: Double?
)
