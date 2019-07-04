package com.app.belcobtm.api.model

import com.google.gson.annotations.SerializedName

/**
 * Created by ADMIN on 17.07.2018.
 */
data class ServerResponse<T>(
        @SerializedName("error") val error: ServerError?,
        @SerializedName("response") var response: T?
)

data class ServerError(
        @SerializedName("errorCode")
        val errorCode: Int, // 2
        @SerializedName("errorMsg")
        val errorMsg: String // Invalid phone number
)