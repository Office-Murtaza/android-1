package com.app.belcobtm.data.rest.settings.request

import com.google.gson.annotations.SerializedName

data class ChangePassBody(
    @SerializedName("newPassword")
    val newPassword: String,
    @SerializedName("oldPassword")
    val oldPassword: String
)