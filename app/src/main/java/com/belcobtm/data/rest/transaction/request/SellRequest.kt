package com.belcobtm.data.rest.transaction.request


data class SellRequest(
    val type: Int,
    val cryptoAmount: Double,
    val price: Double,
    val fiatAmount: Int,
    val serviceFee: Double
)
