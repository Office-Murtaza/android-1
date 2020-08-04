package com.app.belcobtm.data.rest.settings.response

import com.google.gson.annotations.SerializedName

data class GetPhoneResponse(
    @SerializedName("phone")
    val phone: String
)