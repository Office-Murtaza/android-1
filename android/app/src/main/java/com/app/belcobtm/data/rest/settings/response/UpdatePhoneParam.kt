package com.app.belcobtm.data.rest.settings.response

import com.google.gson.annotations.SerializedName

data class UpdatePhoneParam(
    @SerializedName("phone")
    val phone: String
)