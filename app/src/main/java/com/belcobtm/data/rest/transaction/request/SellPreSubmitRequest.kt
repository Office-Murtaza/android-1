package com.belcobtm.data.rest.transaction.request

data class SellPreSubmitRequest(
    val cryptoAmount: Double?,
    val fiatAmount: Int,
    val fiatCurrency: String
)