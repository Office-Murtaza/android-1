package com.app.belcobtm.api.model.response
import com.google.gson.annotations.SerializedName


data class AuthResponse(
    @SerializedName("accessToken")
    val accessToken: String,
    @SerializedName("expires")
    val expires: Long,
    @SerializedName("refreshToken")
    val refreshToken: String,
    @SerializedName("roles")
    val roles: List<String>,
    @SerializedName("userId")
    val userId: Int
)