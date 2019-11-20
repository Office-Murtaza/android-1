package com.app.belcobtm.api.model.param

import com.google.gson.annotations.SerializedName


data class PreTransactionParam(
    @SerializedName("cryptoAmount")
    val cryptoAmount: Double?,

    @SerializedName("fiatAmount")
    val fiatAmount: Int?,

    @SerializedName("fiatCurrency")
    val fiatCurrency: String?
)