package com.belcobtm.data.rest.authorization.request

data class CreateWalletRequest(
    val phone: String,
    val password: String,
    val email: String,
    val deviceModel: String,
    val deviceOS: String,
    val appVersion: String,
    val latitude: Double?,
    val longitude: Double?,
    val timezone: String,
    val notificationToken: String?,
    val coins: List<CreateWalletCoinRequest>,
    val platform: String = PLATFORM,
) {

    companion object {

        private const val PLATFORM = "ANDROID"
    }
}

data class CreateWalletCoinRequest(
    val coin: String,
    val address: String
)
