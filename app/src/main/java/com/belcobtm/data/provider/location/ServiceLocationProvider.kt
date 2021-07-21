package com.belcobtm.data.provider.location

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import android.location.LocationProvider as AndroidLocationProvider

class ServiceLocationProvider(
    private val application: Application,
    private val locationExecutor: Executor
) : LocationProvider {

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
                val gpsProvider: AndroidLocationProvider? =
                    locManager.getProvider(LocationManager.GPS_PROVIDER)
                val networkProvider: AndroidLocationProvider? =
                    locManager.getProvider(LocationManager.NETWORK_PROVIDER)
                if (gpsProvider == null && networkProvider == null) {
                    cont.resume(null)
                    return@suspendCoroutine
                }
                val lastKnownGpsLocation = gpsProvider?.name
                    ?.let(locManager::getLastKnownLocation)
                val lastKnownNetworkLocation = networkProvider?.name
                    ?.let(locManager::getLastKnownLocation)
                if (lastKnownGpsLocation != null) {
                    cont.resume(lastKnownGpsLocation)
                    return@suspendCoroutine
                }
                if (lastKnownNetworkLocation != null) {
                    cont.resume(lastKnownNetworkLocation)
                    return@suspendCoroutine
                }
                val provider = networkProvider?.name ?: gpsProvider?.name
                if (provider != null) {
                    locManager.getCurrentLocation(provider, null, locationExecutor) {
                        cont.resume(it)
                    }
                }
                return@suspendCoroutine
            }
        }
        cont.resume(null)
    }
}