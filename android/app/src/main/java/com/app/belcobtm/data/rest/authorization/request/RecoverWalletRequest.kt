package com.app.belcobtm.data.rest.authorization.request

data class RecoverWalletRequest(
    val phone: String,
    val password: String,
    val deviceModel: String,
    val deviceOS: String,
    val appVersion: String,
    val latitude: Double?,
    val longitude: Double?,
    val timezone: String,
    val notificationsToken: String?,
    val coins: List<RecoverWalletCoinRequest>,
    val platform: Int = 2//android
)

data class RecoverWalletCoinRequest(
    val code: String,
    val address: String
)