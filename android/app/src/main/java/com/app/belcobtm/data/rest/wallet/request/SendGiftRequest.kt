package com.app.belcobtm.data.rest.wallet.request

data class SendGiftRequest (
    val type: Int?,
    val cryptoAmount: Double?,
    val phone: String?,
    val message: String?,
    val imageId: String?,
    val hex: String?
)