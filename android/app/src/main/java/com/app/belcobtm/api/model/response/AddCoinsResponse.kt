package com.app.belcobtm.api.model.response
import com.google.gson.annotations.SerializedName


data class AddCoinsResponse(
    @SerializedName("result")
    val isCoinsAdded: Boolean, // true
    @SerializedName("userId")
    val userId: String // 1000000
)