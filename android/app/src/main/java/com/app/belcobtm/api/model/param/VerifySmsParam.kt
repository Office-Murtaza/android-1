package com.app.belcobtm.api.model.param
import com.google.gson.annotations.SerializedName


data class VerifySmsParam(
    @SerializedName("code")
    val smsCode: String, // 1234
    @SerializedName("userId")
    val userId: String // 1000001
)