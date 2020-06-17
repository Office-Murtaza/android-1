package com.app.belcobtm.presentation.core.extensions

import android.text.Editable

fun Editable.getDouble(): Double = when {
    isEmpty() || (toString().replace("[^\\d]", "").isEmpty()) -> 0.0
    else -> toString().toDouble()
}