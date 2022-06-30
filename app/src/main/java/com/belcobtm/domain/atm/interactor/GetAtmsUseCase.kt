package com.belcobtm.domain.atm.interactor

import android.location.Location
import com.belcobtm.data.helper.DistanceCalculator
import com.belcobtm.data.provider.location.LocationProvider
import com.belcobtm.data.rest.atm.response.AtmAddress
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.atm.AtmRepository
import com.belcobtm.domain.mapSuspend
import com.belcobtm.presentation.screens.atm.AtmItem
import com.belcobtm.presentation.screens.atm.AtmsInfoItem
import com.belcobtm.presentation.screens.atm.OpenHoursItem
import com.belcobtm.presentation.tools.formatter.Formatter
import com.google.android.gms.maps.model.LatLng
import java.util.Calendar

class GetAtmsUseCase(
    private val atmRepository: AtmRepository,
    private val days: Map<Int, String>,
    private val distanceCalculator: DistanceCalculator,
    private val milesFormatter: Formatter<Double>,
    private val locationProvider: LocationProvider
) : UseCase<AtmsInfoItem, Unit>() {

    override suspend fun run(params: Unit): Either<Failure, AtmsInfoItem> =
        atmRepository.getAtms().mapSuspend { response ->
            val calendar = Calendar.getInstance()
            val numOfDay = calendar.get(Calendar.DAY_OF_WEEK)
            val location = locationProvider.getCurrentLocation()
            val atms = response.filter { it.longitude != null && it.latitude != null }
                .map { address ->
                    val daysMap = address.days.filter { it.day != null && it.hours != null }
                        .associateByTo(HashMap(), AtmAddress.OpenDay::day)
                    AtmItem(
                        LatLng(address.latitude ?: 0.0, address.longitude ?: 0.0),
                        address.name,
                        address.address,
                        days.values.toMutableSet().map { openDay ->
                            OpenHoursItem(
                                openDay,
                                daysMap[openDay.lowercase()]?.hours.orEmpty(),
                                openDay.equals(days[numOfDay], ignoreCase = true),
                                daysMap[openDay.lowercase()] == null
                            )
                        },
                        formatDistance(address.latitude ?: 0.0, address.longitude ?: 0.0, location),
                        address.type
                    )
                }
            AtmsInfoItem(atms, location)
        }

    private fun formatDistance(latitude: Double, longitude: Double, location: Location?): String =
        location?.let {
            milesFormatter.format(
                distanceCalculator.calculateDistance(
                    latitude, longitude, it.latitude, it.longitude
                )
            )
        }.orEmpty()

}
