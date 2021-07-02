package com.app.belcobtm.data.provider.location

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ServiceLocationProvider(private val application: Application) : LocationProvider {

    override suspend fun getCurrentLocation(): Location? = suspendCoroutine { cont ->
        val pm = application.packageManager
        val pn = application.packageName
        val fineLocation = pm.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, pn)
        val coarseLocation = pm.checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, pn)
        if (pm.hasSystemFeature(PackageManager.FEATURE_LOCATION)
            && (fineLocation == PackageManager.PERMISSION_GRANTED
                    || coarseLocation == PackageManager.PERMISSION_GRANTED)
        ) {
            val locManager = application.getSystemService(Context.LOCATION_SERVICE)
            if (locManager is LocationManager) {
                val netLocation = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                val gpsLocation = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                cont.resume(gpsLocation ?: netLocation)
                return@suspendCoroutine
            }
        }
        cont.resume(null)
    }
}