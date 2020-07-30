package com.app.belcobtm.api.model.param
import com.google.gson.annotations.SerializedName


@Deprecated("Use CheckPassRequest")
data class CheckPassParam(
    @SerializedName("password")
    val password: String
)