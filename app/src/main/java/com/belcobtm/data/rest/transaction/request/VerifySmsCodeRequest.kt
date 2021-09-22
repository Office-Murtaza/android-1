package com.belcobtm.data.rest.transaction.request

data class VerifySmsCodeRequest(val phone: String, val code: String)