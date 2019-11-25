package com.app.belcobtm.api.model.response
import com.google.gson.annotations.SerializedName


data class CheckPassResponse(
    @SerializedName("result")
    val match: Boolean // true
)