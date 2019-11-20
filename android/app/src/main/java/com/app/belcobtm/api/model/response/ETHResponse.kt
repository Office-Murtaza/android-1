package com.app.belcobtm.api.model.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class ETHResponse(
    @SerializedName("nonce")
    val nonce: Long?

) : Serializable
