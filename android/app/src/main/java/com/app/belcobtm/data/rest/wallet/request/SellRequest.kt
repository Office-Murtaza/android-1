package com.app.belcobtm.data.rest.wallet.request


data class SellRequest(
    val type: Int,
    val cryptoAmount: Double,
    val hex: String
)
