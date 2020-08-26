package com.app.belcobtm.data.rest.atm.response

data class AtmResponse(
    val addresses: List<AtmAddress>
) {
    data class AtmAddress(
        val id: Int,
        val name: String,
        val address: String,
        val latitude: Double,
        val longitude: Double,
        val hours: List<OpenHour>
    ) {
        data class OpenHour(
            val days: String,
            val hours: String,
            val id: Int
        )
    }
}