package com.app.belcobtm.api.model.response
import com.google.gson.annotations.SerializedName


data class RequestSmsResponse(
    @SerializedName("sent")
    val sent: Boolean // true
)