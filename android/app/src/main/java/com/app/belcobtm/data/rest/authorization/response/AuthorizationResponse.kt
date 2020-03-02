package com.app.belcobtm.data.rest.authorization.response

data class AuthorizationResponse(
    val accessToken: String,
    val expires: Long,
    val refreshToken: String,
    val roles: List<String>,
    val userId: Int
)