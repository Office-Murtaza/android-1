package com.app.belcobtm.api.model.response

import com.google.gson.annotations.SerializedName


data class UpdateResponse(
    @SerializedName("result")
    val updated: Boolean // true
    ,
    @SerializedName("txId")
    var txId: String? = null
)