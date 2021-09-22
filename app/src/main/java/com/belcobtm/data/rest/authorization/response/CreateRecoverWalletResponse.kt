package com.belcobtm.data.rest.authorization.response

import com.belcobtm.data.rest.service.ServiceFeeResponse
import com.belcobtm.data.rest.wallet.response.BalanceResponse

data class CreateRecoverWalletResponse(
    val userId: String,
    val identityId: Int,
    val accessToken: String,
    val expires: Long,
    val refreshToken: String,
    val firebaseToken: String,
    val roles: List<String>?,
    val referralCode: String?,
    val referralInvites: Int?,
    val referralEarned: Int?,
    val balance: BalanceResponse,
    val services: List<Int>,
    val serviceFees: List<ServiceFeeResponse>
)