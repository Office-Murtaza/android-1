package com.app.belcobtm.api.model.param

import com.google.gson.annotations.SerializedName


data class UpdatePhoneParam(
    @SerializedName("phone")
    val phone: String // abc123456
)