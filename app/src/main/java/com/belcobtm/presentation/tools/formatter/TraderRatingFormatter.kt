package com.belcobtm.presentation.tools.formatter

import java.text.NumberFormat
import java.util.Locale

class TraderRatingFormatter(locale: Locale) : Formatter<Double> {

    private val numberFormat: NumberFormat = NumberFormat.getCurrencyInstance(locale)

    override fun format(input: Double): String = numberFormat.format(input)
        .replace("[.]00".toRegex(), "")
        .removeSuffix("0")
        .removeSuffix(".0")

    companion object {

        const val TRADER_RATING_FORMATTER_QUALIFIER = "TraderRatingFormatter"
    }

}
