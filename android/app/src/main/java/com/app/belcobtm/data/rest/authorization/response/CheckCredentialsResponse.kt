package com.belcobtm.data.rest.authorization.response

class CheckCredentialsResponse(
    val passwordMatch: Boolean,
    val phoneExist: Boolean
)