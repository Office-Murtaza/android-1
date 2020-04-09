package com.app.belcobtm.presentation.core.extensions


fun Double.toStringUsd(): String = trimValue(2, this)

fun Double.toStringCoin(): String = trimValue(6, this)

private fun trimValue(charsAfterDot: Int, value: Double): String = if (value > 0) {
    val trimmed = String.format("%." + charsAfterDot + "f", value).trimEnd('0')
    if (trimmed.last() == ',') {
        trimmed.trimEnd(',')
    } else {
        trimmed.replaceFirst(',', '.')
    }
} else {
    "0"
}