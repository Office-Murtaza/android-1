package com.app.belcobtm.presentation.core.extensions

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

fun Double.toStringUsd(): String = if (this > 0) {
    val df = DecimalFormat("#.###")
    df.roundingMode = RoundingMode.FLOOR
    val trimmed = df.format(this)
    if (trimmed.isNotBlank()) {
        when (trimmed.last()) {
            ',' -> trimmed.trimEnd(',')
            '.' -> trimmed.trimEnd('.')
            else -> trimmed.replaceFirst(',', '.')
        }
    } else {
        "0"
    }
} else {
    "0"
}

fun Double.toStringCoin(): String = if (this > 0) {
    val df = DecimalFormat("#.######")
    df.roundingMode = RoundingMode.FLOOR
    val trimmed = df.format(this)
    if (trimmed.isNotBlank()) {
        when (trimmed.last()) {
            ',' -> trimmed.trimEnd(',')
            '.' -> trimmed.trimEnd('.')
            else -> trimmed.replaceFirst(',', '.')
        }
    } else {
        "0"
    }
} else {
    "0"
}

fun Double.truncateDecimal(numberOfDecimals: Int): Double {
    return if (this > 0) {
        BigDecimal(this.toString()).setScale(numberOfDecimals, BigDecimal.ROUND_FLOOR)
    } else {
        BigDecimal(this.toString()).setScale(numberOfDecimals, BigDecimal.ROUND_CEILING)
    }.toDouble()
}