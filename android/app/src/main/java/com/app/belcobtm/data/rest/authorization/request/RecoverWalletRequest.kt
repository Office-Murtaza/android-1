package com.app.belcobtm.data.rest.authorization.request

data class RecoverWalletRequest(
    val phone: String,
    val password: String,
    val deviceModel: String,
    val deviceOS: String,
    val appVersion: String,
    val notificationsToken: String?,
    val coins: List<RecoverWalletCoinRequest>,
    val platform: Int = 2, //android
    val latitude: Double = 48.4577020796,
    val longitude: Double = 35.0733159377,
    val timezone: String = "GMT+3"
)

data class RecoverWalletCoinRequest(
    val code: String,
    val address: String
)