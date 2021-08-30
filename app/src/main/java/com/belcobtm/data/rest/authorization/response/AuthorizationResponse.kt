package com.belcobtm.data.rest.authorization.response

import com.belcobtm.data.rest.service.ServiceFeeResponse

data class AuthorizationResponse(
    val accessToken: String,
    val expires: Long,
    val refreshToken: String,
    val firebaseToken: String,
    val roles: List<String>,
    val userId: String,
    val referralCode: String?,
    val referralInvites: Int?,
    val referralEarned: Int?,
    val services: List<Int>,
    val fees: List<ServiceFeeResponse>,
)