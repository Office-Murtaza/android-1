package com.belcobtm.presentation.tools.formatter

import java.text.NumberFormat
import java.util.Locale

class CurrencyPriceFormatter(locale: Locale) : Formatter<Double> {

    private val numberFormat: NumberFormat = NumberFormat.getCurrencyInstance(locale)

    override fun format(input: Double): String = numberFormat.format(input)
        .replace("$[.]00".toRegex(), "")
        .replace(".00", "")

    companion object {

        const val CURRENCY_PRICE_FORMATTER_QUALIFIER = "CurrencyPriceFormatter"
    }

}
