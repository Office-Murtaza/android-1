package com.belcobtm.presentation.core.extensions

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

fun Double.toStringPercents(): String = if (this > 0) {
    formatWith(DecimalFormat("#.##"))
} else {
    "0"
}

fun Double.toStringUsd(): String = if (this > 0) {
    formatWith(DecimalFormat("#.###"))
} else {
    "0"
}

fun Double.toStringCoin(): String = if (this > 0) {
    formatWith(DecimalFormat("#.######"))
} else {
    "0"
}

private fun Double.formatWith(df: DecimalFormat): String {
    df.roundingMode = RoundingMode.FLOOR
    val trimmed = df.format(this)
    return if (trimmed.isNotBlank()) {
        when (trimmed.last()) {
            ',' -> trimmed.trimEnd(',')
            '.' -> trimmed.trimEnd('.')
            else -> trimmed.replaceFirst(',', '.')
        }
    } else {
        "0"
    }
}

fun Double.withScale(scale: Int): Double {
    return BigDecimal(this).setScale(scale, RoundingMode.DOWN).toDouble()
}