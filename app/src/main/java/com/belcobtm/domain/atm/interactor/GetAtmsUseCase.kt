package com.belcobtm.domain.atm.interactor

import com.belcobtm.data.helper.DistanceCalculator
import com.belcobtm.data.provider.location.LocationProvider
import com.belcobtm.data.rest.atm.response.AtmResponse
import com.belcobtm.domain.*
import com.belcobtm.domain.atm.AtmRepository
import com.belcobtm.presentation.core.formatter.Formatter
import com.belcobtm.presentation.features.atm.AtmItem
import com.belcobtm.presentation.features.atm.OpenHoursItem
import com.google.android.gms.maps.model.LatLng
import java.util.*
import kotlin.collections.HashMap

class GetAtmsUseCase(
    private val atmRepository: AtmRepository,
    private val days: Map<Int, String>,
    private val distanceCalculator: DistanceCalculator,
    private val milesFormatter: Formatter<Double>,
    private val locationProvider: LocationProvider
) : UseCase<List<AtmItem>, Unit>() {

    override suspend fun run(params: Unit): Either<Failure, List<AtmItem>> =
        atmRepository.getAtms().mapSuspend { response ->
            val calendar = Calendar.getInstance()
            val numOfDay = calendar.get(Calendar.DAY_OF_WEEK)
            response.locations.map { address ->
                val daysMap =
                    address.days.associateByTo(HashMap(), AtmResponse.AtmAddress.OpenDay::day)
                AtmItem(
                    LatLng(address.latitude, address.longitude),
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
                    formatDistance(address.latitude, address.longitude),
                    address.operation
                )
            }
        }

    private suspend fun formatDistance(latitude: Double, longitude: Double): String =
        locationProvider.getCurrentLocation()?.let {
            milesFormatter.format(
                distanceCalculator.calculateDistance(
                    latitude, longitude, it.latitude, it.longitude
                )
            )
        }.orEmpty()
}