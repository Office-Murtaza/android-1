package com.app.belcobtm.data.rest.wallet.request

data class SellPreSubmitRequest(
    val cryptoAmount: Double?,
    val fiatAmount: Int,
    val fiatCurrency: String
)