package com.app.belcobtm.data.rest.wallet.request

data class WithdrawRequest(
    val type: Int,
    val cryptoAmount: Double,
    val hex: String
)