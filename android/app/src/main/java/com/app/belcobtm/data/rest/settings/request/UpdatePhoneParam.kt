package com.app.belcobtm.data.rest.settings.request

import com.google.gson.annotations.SerializedName

data class UpdatePhoneParam(
    @SerializedName("phone")
    val phone: String
)