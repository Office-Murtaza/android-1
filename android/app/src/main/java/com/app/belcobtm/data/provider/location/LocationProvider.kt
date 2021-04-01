package com.app.belcobtm.data.provider.location

import android.location.Location

interface LocationProvider {

    suspend fun getCurrentLocation(): Location?
}