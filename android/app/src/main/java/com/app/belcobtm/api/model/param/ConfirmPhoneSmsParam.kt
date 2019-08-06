package com.app.belcobtm.api.model.param

import com.google.gson.annotations.SerializedName

data class ConfirmPhoneSmsParam(
    @SerializedName("phone")
    val phone: String, // abc123456d
    @SerializedName("code")
    val code: String // 4590
)