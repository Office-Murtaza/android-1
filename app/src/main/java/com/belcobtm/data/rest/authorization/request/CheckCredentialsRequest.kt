package com.belcobtm.data.rest.authorization.request

data class CheckCredentialsRequest(
    val phone: String,
    val password: String,
    val email: String
)