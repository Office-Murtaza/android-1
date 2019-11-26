package com.app.belcobtm.api.model.response
import com.google.gson.annotations.SerializedName


data class AddCoinsResponse(
    @SerializedName("result")
    val isCoinsAdded: Boolean,
    @SerializedName("userId")
    val userId: String
)