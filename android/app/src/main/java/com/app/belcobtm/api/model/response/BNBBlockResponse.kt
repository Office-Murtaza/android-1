package com.app.belcobtm.api.model.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class BNBBlockResponse(
    @SerializedName("accountNumber")
    val accountNumber: Long?,
    @SerializedName("sequence")
    val sequence: Long?

) : Serializable
