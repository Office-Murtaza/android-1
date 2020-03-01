package com.app.belcobtm.data.rest.authorization.response

data class RegisterWalletResponse(
    val accessToken: String,
    val expires: Long,
    val refreshToken: String,
    val roles: List<String>,
    val userId: Int
)