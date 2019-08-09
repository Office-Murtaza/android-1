package com.app.belcobtm.api.model.param
import com.google.gson.annotations.SerializedName


data class CheckPassParam(
    @SerializedName("password")
    val password: String // abc123456
)