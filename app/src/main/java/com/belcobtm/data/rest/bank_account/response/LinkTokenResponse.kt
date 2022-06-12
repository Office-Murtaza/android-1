package com.belcobtm.data.rest.bank_account.response

data class LinkTokenResponse(
    val linkToken: String?,
    val expiration: String?,
    val requestId: String?
)