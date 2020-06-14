package com.app.belcobtm.data.rest.transaction.response

import com.app.belcobtm.domain.transaction.item.SellPreSubmitDataItem

data class SellPreSubmitResponse(
    val cryptoAmount: Double?,
    var address: String?
)

fun SellPreSubmitResponse.mapToDataItem(): SellPreSubmitDataItem = SellPreSubmitDataItem(
    fromCoinAmount = cryptoAmount ?: 0.0,
    address = address ?: ""
)