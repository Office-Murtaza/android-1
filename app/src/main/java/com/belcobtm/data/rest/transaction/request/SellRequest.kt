package com.belcobtm.data.rest.transaction.request

import com.belcobtm.domain.transaction.type.TransactionType

data class SellRequest(
    val type: String = TransactionType.ATM_SELL.toString(),
    val cryptoAmount: Double,
    val price: Double,
    val fiatAmount: Int,
    val feePercent: Double,
    val latitude: Double?,
    val longitude: Double?
)
