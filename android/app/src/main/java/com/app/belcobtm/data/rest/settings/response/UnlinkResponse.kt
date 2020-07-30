package com.app.belcobtm.data.rest.settings.response

import com.google.gson.annotations.SerializedName

data class UnlinkResponse(
    @SerializedName("result")
    val result: Boolean
)