package com.app.belcobtm.api.model.response
import com.google.gson.annotations.SerializedName


data class GetPhoneResponse(
    @SerializedName("phone")
    val phone: String
)