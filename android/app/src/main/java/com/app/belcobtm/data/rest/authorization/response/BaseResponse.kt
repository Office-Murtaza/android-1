package com.app.belcobtm.data.rest.authorization.response

abstract class BaseResponse{
    abstract val error: ErrorMessage?
}

data class ErrorMessage(val errorMsg: String?)