package com.app.belcobtm.api.model.response
import com.google.gson.annotations.SerializedName


data class ConfirmPhoneSmsResponse(
    @SerializedName("result")
    val confirmed: Boolean // true
)