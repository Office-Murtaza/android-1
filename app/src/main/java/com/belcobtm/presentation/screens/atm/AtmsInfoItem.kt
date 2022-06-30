package com.belcobtm.presentation.screens.atm

import android.location.Location

data class AtmsInfoItem(
    val atms: List<AtmItem>,
    val location: Location?
)