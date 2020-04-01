package com.app.belcobtm.api.model.response

data class GetCoinFeeResponse(
    val txFee: Double,
    val byteFee: Double?,
    val gasPrice: Double?,
    val gasLimit: Double?,
    val profitC2C: Double
)