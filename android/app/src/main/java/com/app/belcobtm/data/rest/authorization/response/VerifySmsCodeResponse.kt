package com.app.belcobtm.data.rest.authorization.response

data class VerifySmsCodeResponse(
    override val error: ErrorMessage?
): BaseResponse()