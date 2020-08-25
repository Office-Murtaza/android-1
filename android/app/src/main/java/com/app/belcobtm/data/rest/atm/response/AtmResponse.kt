package com.app.belcobtm.data.rest.atm.response

data class AtmResponse(
    val addresses: ArrayList<AtmAddress>
) {
    data class AtmAddress(
        val address: String,
        val id: Int,
        val latitude: Double,
        val name: String,
        val longitude: Double,
        val hours: ArrayList<OpenHour>
    ) {
        data class OpenHour(
            val days: String,
            val hours: String,
            val id: Int
        )
    }
}