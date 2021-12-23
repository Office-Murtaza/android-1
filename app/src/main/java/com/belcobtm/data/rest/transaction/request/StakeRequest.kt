package com.belcobtm.data.rest.transaction.request

class StakeRequest(
    val type: Int,
    val fromAddress: String,
    val toAddress: String,
    val cryptoAmount: Double,
    val fee: Double,
    val hex: String,
    val feePercent: Double? = null,
    val fiatAmount: Double? = null,
)