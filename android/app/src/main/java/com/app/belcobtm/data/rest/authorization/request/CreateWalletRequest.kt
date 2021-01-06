package com.app.belcobtm.data.rest.authorization.request

data class CreateWalletRequest(
    val phone: String,
    val password: String,
    val deviceModel: String,
    val deviceOS: String,
    val appVersion: String,
    val notificationsToken: String?,
    val coins: List<CreateWalletCoinRequest>,
    val platform: Int = 2//android
)

data class CreateWalletCoinRequest(
    val code: String,
    val address: String
)