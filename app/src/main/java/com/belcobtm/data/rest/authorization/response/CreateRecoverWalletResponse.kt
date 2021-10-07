package com.belcobtm.data.rest.authorization.response

import com.belcobtm.data.rest.service.ServiceFeeResponse
import com.belcobtm.data.rest.wallet.response.BalanceResponse

data class CreateRecoverWalletResponse(
    val accessToken: String,
    val expires: Long,
    val refreshToken: String,
    val firebaseToken: String,
    val user: UserDetailsResponse,
    val balance: BalanceResponse,
    val serviceFees: List<ServiceFeeResponse>
)

data class UserDetailsResponse(
    val id: String,
    val roles: List<String>?,
    val referralCode: String?,
    val referrals: Int?,
    val referralEarned: Int?,
    val availableServices: List<Int>
)