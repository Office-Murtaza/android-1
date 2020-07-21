package com.app.belcobtm.data.rest.authorization.response

data class CreateRecoverWalletResponse(
    val userId: Int,
    val identityId: Int,
    val accessToken: String,
    val expires: Long,
    val refreshToken: String,
    val roles: List<String>?,
    val balance: RecoverWalletBalanceResponse
)

data class RecoverWalletBalanceResponse(
    val totalBalance: Double,
    val coins: List<RecoverWalletCoinResponse>
)

data class RecoverWalletCoinResponse(
    val id: Int,
    val code: String,
    val address: String,
    val balance: Double,
    val reservedBalance: Double,
    val price: Double
)
