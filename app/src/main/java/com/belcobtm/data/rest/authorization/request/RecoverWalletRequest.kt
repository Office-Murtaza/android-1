package com.belcobtm.data.rest.authorization.request

data class RecoverWalletRequest(
    val phone: String,
    val password: String,
    val deviceModel: String,
    val deviceOS: String,
    val appVersion: String,
    val latitude: Double?,
    val longitude: Double?,
    val timezone: String,
    val referralCodeFrom: String?,
    val notificationToken: String?,
    val coins: List<RecoverWalletCoinRequest>,
    val platform: Int = 2, //android
)

data class RecoverWalletCoinRequest(
    val coin: String,
    val address: String
)