package com.app.belcobtm.api.model.response
import com.google.gson.annotations.SerializedName


data class AtmResponse(
    @SerializedName("addresses")
    val atmAddressList: ArrayList<AtmAddress>
) {
    data class AtmAddress(
        @SerializedName("address")
        val address: String, // 30 Paterson St New Brunswick, New Jersey, 08901
        @SerializedName("id")
        val id: Int, // 13
        @SerializedName("latitude")
        val latitude: Double, // 40.50
        @SerializedName("name")
        val locationName: String, // Express Smoke and Vape Shop
        @SerializedName("longitude")
        val longitude: Double, // -74.44
        @SerializedName("hours")
        val openHours: ArrayList<OpenHour>
    ) {
        data class OpenHour(
            @SerializedName("days")
            val days: String, // Sun
            @SerializedName("hours")
            val hours: String, // 10:00am-11:00pm
            @SerializedName("id")
            val id: Int // 27
        )
    }
}