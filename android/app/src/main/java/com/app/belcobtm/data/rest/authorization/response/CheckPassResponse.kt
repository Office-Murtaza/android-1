package com.app.belcobtm.data.rest.authorization.response

import com.google.gson.annotations.SerializedName

data class CheckPassResponse(
    @SerializedName("result")
    val match: Boolean
)