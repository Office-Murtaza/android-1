package com.belcobtm.presentation.features.atm

import com.google.android.gms.maps.model.LatLng

data class AtmItem(val latLng: LatLng, val title: String, val address: String)