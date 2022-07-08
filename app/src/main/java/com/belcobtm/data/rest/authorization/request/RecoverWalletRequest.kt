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
    val notificationToken: String?,
    val coins: List<RecoverWalletCoinRequest>,
    val platform: String = PLATFORM,
) {

    companion object {

        private const val PLATFORM = "ANDROID"
    }
}

data class RecoverWalletCoinRequest(
    val coin: String,
    val address: String
)
