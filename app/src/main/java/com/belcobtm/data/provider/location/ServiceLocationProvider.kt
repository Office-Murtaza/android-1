package com.belcobtm.data.provider.location

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import java.util.concurrent.Executor
import java.util.function.Consumer
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

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
                val criteria = Criteria().apply {
                    accuracy = Criteria.ACCURACY_COARSE
                }
                val provider: String? = locManager.getBestProvider(criteria, true)
                if (provider == null) {
                    cont.resume(null)
                    return@suspendCoroutine
                }
                locManager.getCurrentLocation(provider, null, locationExecutor) {
                    cont.resume(it)
                }
                return@suspendCoroutine
            }
        }
        cont.resume(null)
    }
}