package com.belcobtm.data.rest.transaction.request

data class SendGiftRequest(
    val type: Int?,
    val cryptoAmount: Double?,
    val phone: String?,
    val message: String?,
    val image: String?,
    val hex: String?,
    val fee: Double?,
    val feePercent: Int?,
    val fiatAmount: Double?,
    val fromAddress: String?,
    val toAddress: String?,
    val longitude: Double,
    val latitude: Double
)