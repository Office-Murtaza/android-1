package com.app.belcobtm.api.model.param

import com.google.gson.annotations.SerializedName


data class AuthParam(
    @SerializedName("phone")
    val phone: String,
    @SerializedName("password")
    val password: String
)