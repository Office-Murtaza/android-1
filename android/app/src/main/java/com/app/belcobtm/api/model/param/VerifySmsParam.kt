package com.app.belcobtm.api.model.param

import com.google.gson.annotations.SerializedName


data class VerifySmsParam(
    @SerializedName("code")
    val smsCode: String
)