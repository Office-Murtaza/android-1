package com.app.belcobtm.data.rest.authorization.request

data class CheckCredentialsRequest(
    val phone: String,
    val password: String
)