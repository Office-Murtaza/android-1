package com.belcobtm.data.rest.transaction.request

import com.belcobtm.domain.transaction.type.TransactionType

data class SendGiftRequest(
    val type: String = TransactionType.SEND_TRANSFER.toString(),
    val cryptoAmount: Double?,
    val phone: String?,
    val message: String?,
    val image: String?,
    val hex: String?,
    val price: Double,
    val fee: Double?,
    val feePercent: Int?,
    val fiatAmount: Double?,
    val fromAddress: String?,
    val toAddress: String?,
    val longitude: Double,
    val latitude: Double
)
