package com.app.belcobtm.data.rest.transaction.request

data class WithdrawRequest(
    val type: Int,
    val cryptoAmount: Double,
    val hex: String
)