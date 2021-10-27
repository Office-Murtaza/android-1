package com.belcobtm.data.rest.atm.response

data class AtmAddress(
    val id: String,
    val name: String,
    val address: String,
    val city: String,
    val country: String,
    val latitude: Double?,
    val longitude: Double?,
    @OperationType val type: Int,
    val days: List<OpenDay>
) {
    data class OpenDay(
        val day: String?,
        val hours: String?
    )
}