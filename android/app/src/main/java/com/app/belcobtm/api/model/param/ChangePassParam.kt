package com.app.belcobtm.api.model.param

import com.google.gson.annotations.SerializedName


data class ChangePassParam(
    @SerializedName("newPassword")
    val newPassword: String,
    @SerializedName("oldPassword")
    val oldPassword: String
)