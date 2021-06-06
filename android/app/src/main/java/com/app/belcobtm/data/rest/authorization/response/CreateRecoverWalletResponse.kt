package com.app.belcobtm.data.rest.authorization.response

import com.app.belcobtm.data.rest.wallet.response.BalanceResponse

data class CreateRecoverWalletResponse(
    val userId: Int,
    val identityId: Int,
    val accessToken: String,
    val expires: Long,
    val refreshToken: String,
    val firebaseToken: String,
    val roles: List<String>?,
    val balance: BalanceResponse
)