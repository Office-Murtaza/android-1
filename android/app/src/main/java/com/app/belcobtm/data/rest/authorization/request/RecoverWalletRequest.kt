package com.app.belcobtm.data.rest.authorization.request

data class RecoverWalletRequest(
    val phone: String,
    val password: String,
    val coins: Map<String, String>,
    val platform: Int = 1//android
)