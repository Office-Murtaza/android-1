package com.belcobtm.data.rest.transaction.request

data class SendGiftRequest(
    val type: Int?,
    val cryptoAmount: Double?,
    val phone: String?,
    val message: String?,
    val image: String?,
    val hex: String?,
    val fee: Double?,
    val fromAddress: String?,
    val toAddress: String?
)