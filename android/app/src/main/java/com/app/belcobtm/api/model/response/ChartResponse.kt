package com.app.belcobtm.api.model.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ChartResponse(
    @SerializedName("price") val price: Double,
    @SerializedName("balance") val balance: Double,
    @SerializedName("chart") val chart: ChartInfoListResponse
) : Serializable

data class ChartInfoListResponse(
    @SerializedName("day") val day: ChartInfoResponse,
    @SerializedName("week") val week: ChartInfoResponse,
    @SerializedName("month") val month: ChartInfoResponse,
    @SerializedName("threeMonths") val threeMonths: ChartInfoResponse,
    @SerializedName("year") val year: ChartInfoResponse
)

data class ChartInfoResponse(
    @SerializedName("prices") val prices: List<Double>,
    @SerializedName("changes") val changes: Double
) : Serializable