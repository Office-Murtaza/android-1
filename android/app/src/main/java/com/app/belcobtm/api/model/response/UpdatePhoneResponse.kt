package com.app.belcobtm.api.model.response
import com.google.gson.annotations.SerializedName


data class UpdatePhoneResponse(
    @SerializedName("result")
    val smsSent: Boolean // true
)