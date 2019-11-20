package com.app.belcobtm.api.model.response

import com.google.gson.annotations.SerializedName


data class GiftAddressResponse(
    @SerializedName("address")
    val address: String?,
    @SerializedName("exist")
    val exist: Boolean?
)