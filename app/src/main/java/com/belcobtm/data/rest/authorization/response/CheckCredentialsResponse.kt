package com.belcobtm.data.rest.authorization.response

class CheckCredentialsResponse(
    val passwordsMatch: Boolean,
    val phoneExists: Boolean
)