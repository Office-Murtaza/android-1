package com.belcobtm.data.rest.transaction.request

data class StakeRequest(
    val type: String,
    val fromAddress: String,
    val toAddress: String,
    val cryptoAmount: Double,
    val fee: Double,
    val hex: String,
    val longitude: Double,
    val latitude: Double,
    val feePercent: Double? = null,
    val fiatAmount: Double? = null,
)
