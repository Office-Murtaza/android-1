package com.app.belcobtm.data.rest.authorization.request

data class RecoverWalletRequest(
    val phone: String,
    val password: String,
    val coins: List<RecoverWalletCoinRequest>,
    val platform: Int = 2//android
)

data class RecoverWalletCoinRequest(
    val code: String,
    val address: String
)