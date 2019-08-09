package com.app.belcobtm.api.model.response
import com.google.gson.annotations.SerializedName


data class ConfirmPhoneSmsResponse(
    @SerializedName("confirmed")
    val confirmed: Boolean // true
)