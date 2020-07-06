package com.app.belcobtm.data.rest.authorization.request

data class CreateWalletRequest(
    val phone: String,
    val password: String,
    val platform: Int = 1 //android
)