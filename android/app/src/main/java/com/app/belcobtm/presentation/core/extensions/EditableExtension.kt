package com.app.belcobtm.presentation.core.extensions

import android.text.Editable

fun Editable.getDouble(): Double = when {
    isEmpty() || (toString().replace("[^\\d]", "").isEmpty()) -> 0.0
    else -> toString().toDouble()
}

fun Editable.getDouble(regexToRemove: String): Double = when {
    isEmpty() || (toString().replace("[^\\d]", "").isEmpty()) -> 0.0
    else -> toString().replace(regexToRemove.toRegex(), "").ifEmpty { "0" }.toDouble()
}

fun Editable.getInt(): Int = when {
    isEmpty() || (toString().replace("[^\\d]", "").isEmpty()) -> 0
    else -> toString().toInt()
}