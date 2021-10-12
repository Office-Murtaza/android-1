package com.belcobtm.data.rest.authorization.response

import com.belcobtm.data.rest.service.ServiceFeeResponse
import com.belcobtm.data.rest.wallet.response.BalanceResponse

data class AuthorizationResponse(
    val accessToken: String,
    val expires: Long,
    val refreshToken: String,
    val firebaseToken: String,
    val user: UserDetailsResponse,
    val serviceFees: List<ServiceFeeResponse>
)