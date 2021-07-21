package com.belcobtm.presentation.features.atm

import android.location.Location

data class AtmsInfoItem(
    val atms: List<AtmItem>,
    val location: Location?
)