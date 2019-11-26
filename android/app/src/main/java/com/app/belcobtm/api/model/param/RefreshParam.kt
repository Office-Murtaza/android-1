package com.app.belcobtm.api.model.param

import com.google.gson.annotations.SerializedName


data class RefreshParam(
    @SerializedName("refreshToken")
    val refreshToken: String?
)