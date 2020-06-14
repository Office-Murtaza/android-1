package com.app.belcobtm.domain.transaction.item

data class SellPreSubmitDataItem(
    val fromCoinAmount: Double,
    var address: String
)