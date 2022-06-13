package com.belcobtm.data.rest.authorization.response

import com.belcobtm.data.rest.wallet.response.BalanceResponse
import com.belcobtm.data.websockets.services.model.ServicesInfoResponse

data class CreateRecoverWalletResponse(
    val accessToken: String,
    val expires: Long,
    val refreshToken: String,
    val firebaseToken: String,
    val zendeskToken: String,
    val user: UserDetailsResponse,
    val balance: BalanceResponse,
    val services: List<ServicesInfoResponse>
)

data class UserDetailsResponse(
    val id: String,
    val roles: List<String>?,
    val referralCode: String?,
    val referrals: Int?,
    val referralEarned: Double?,
    val status: String
)