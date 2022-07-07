package com.belcobtm.data.rest.transaction.request

import com.belcobtm.domain.transaction.type.TransactionType

data class WithdrawRequest(
    val type: String = TransactionType.WITHDRAW.toString(),
    val hex: String,
    val fromAddress: String?,
    val toAddress: String?,
    val cryptoAmount: Double,
    val fee: Double?,
    val price: Double,
    val latitude: Double?,
    val longitude: Double?,
    val fiatAmount: Double
)
