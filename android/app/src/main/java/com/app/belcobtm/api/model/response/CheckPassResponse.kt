package com.app.belcobtm.api.model.response
import com.google.gson.annotations.SerializedName


@Deprecated("Use new CheckPassResponse from rest package instead")
data class CheckPassResponse(
    @SerializedName("result")
    val match: Boolean
)