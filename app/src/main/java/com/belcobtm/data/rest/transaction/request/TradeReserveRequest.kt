package com.belcobtm.data.rest.transaction.request

import com.belcobtm.domain.transaction.type.TransactionType

data class TradeReserveRequest(
    val type: String = TransactionType.RESERVE.toString(),
    val fromAddress: String,
    val toAddress: String,
    val cryptoAmount: Double,
    val fee: Double,
    val hex: String,
    val price: Double,
    val fiatAmount: Double,
    val latitude: Double?,
    val longitude: Double?
)
