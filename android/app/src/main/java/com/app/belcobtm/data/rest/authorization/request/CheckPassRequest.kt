package com.app.belcobtm.data.rest.authorization.request

import com.google.gson.annotations.SerializedName

data class CheckPassRequest(
    @SerializedName("password")
    val password: String
)