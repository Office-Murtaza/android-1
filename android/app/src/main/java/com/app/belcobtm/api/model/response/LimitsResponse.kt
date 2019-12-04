package com.app.belcobtm.api.model.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class LimitsResponse(
    @SerializedName("dailyLimit")
    val dailyLimit: Limit?,
    @SerializedName("txLimit")
    val txLimit: Limit?,
    @SerializedName("sellProfitRate")
    val sellProfitRate: Double

) : Serializable


data class Limit(
    @SerializedName("USD")
    val USD: Double?
) : Serializable