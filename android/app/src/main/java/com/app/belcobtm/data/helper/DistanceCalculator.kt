package com.app.belcobtm.data.helper

import com.app.belcobtm.data.model.trade.Trade
import com.app.belcobtm.data.provider.location.LocationProvider
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class DistanceCalculator(
    private val locationProvider: LocationProvider
) {

    private companion object {
        const val EARTH_RADIUS_KM = 6371
        const val KM_TO_MILES_MULTIPLIER = 0.62137
    }

    suspend fun updateDistanceToTrades(trades: MutableMap<String, Trade>): MutableMap<String, Trade> {
        val currentLocation = locationProvider.getCurrentLocation() ?: return trades
        val currentLat = currentLocation.latitude
        val currentLong = currentLocation.longitude
        return trades.mapValuesTo(HashMap()) {
            val trade = it.value
            val makerLat = trade.makerLatitude
            val makerLong = trade.makerLongitude
            if (makerLat != null && makerLong != null) {
                trade.copy(distance = calculateDistance(currentLat, currentLong, makerLat, makerLong))
            } else {
                trade
            }
        }
    }

    private fun calculateDistance(fromLat: Double, fromLong: Double, toLat: Double, toLong: Double): Double {
        val dLat = degreesToRadians(toLat - fromLat)
        val dLon = degreesToRadians(toLong - fromLong)

        val fromLatRadians = degreesToRadians(fromLat)
        val toLatRadians = degreesToRadians(toLat)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                sin(dLon / 2) * sin(dLon / 2) * cos(fromLatRadians) * cos(toLatRadians)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_KM * c * KM_TO_MILES_MULTIPLIER
    }

    private fun degreesToRadians(degrees: Double) = degrees * Math.PI / 180
}