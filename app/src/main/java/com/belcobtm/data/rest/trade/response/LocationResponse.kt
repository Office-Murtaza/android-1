package com.belcobtm.data.rest.trade.response

data class LocationResponse(
    val ipAddress: String?,
    val latitude: Double?,
    val longitude: Double?,
    val country: String?,
    val province: String?,
    val city: String?,
    val zip: String?,
    val address: String?
)
