package com.app.belcobtm.api.model.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class PreSubmitResponse(
    @SerializedName("cryptoAmount")
    val cryptoAmount: Double?,
    @SerializedName("address")
    var address: String? = null
) : Serializable