package com.app.belcobtm.presentation.core.formatter

class GoogleMapsDirectionQueryFormatter : Formatter<GoogleMapsDirectionQueryFormatter.Location> {

    companion object {
        const val GOOGLE_MAPS_DIRECTIONS_QUERY_FORMATTER = "GoogleMapsDirectionQueryFormatter"
    }

    override fun format(input: Location): String =
        "google.navigation:q=${input.latitude},${input.longitude}"

    data class Location(val latitude: Double, val longitude: Double)
}