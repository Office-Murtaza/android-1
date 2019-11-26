package com.app.belcobtm.api.model.response
import com.google.gson.annotations.SerializedName


data class AtmResponse(
    @SerializedName("addresses")
    val atmAddressList: ArrayList<AtmAddress>
) {
    data class AtmAddress(
        @SerializedName("address")
        val address: String,
        @SerializedName("id")
        val id: Int,
        @SerializedName("latitude")
        val latitude: Double,
        @SerializedName("name")
        val locationName: String,
        @SerializedName("longitude")
        val longitude: Double,
        @SerializedName("hours")
        val openHours: ArrayList<OpenHour>
    ) {
        data class OpenHour(
            @SerializedName("days")
            val days: String,
            @SerializedName("hours")
            val hours: String,
            @SerializedName("id")
            val id: Int
        )
    }
}