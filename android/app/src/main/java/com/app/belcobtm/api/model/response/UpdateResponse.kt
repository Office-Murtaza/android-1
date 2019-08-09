package com.app.belcobtm.api.model.response
import com.google.gson.annotations.SerializedName


data class UpdateResponse(
    @SerializedName("updated")
    val updated: Boolean // true
)