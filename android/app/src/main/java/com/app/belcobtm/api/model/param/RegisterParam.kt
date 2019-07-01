package com.app.belcobtm.api.model.param

import com.google.gson.annotations.SerializedName


data class RegisterParam(
    @SerializedName("phone")
    val phone: String, // +12018906708
    @SerializedName("password")
    val password: String // abc123456
)