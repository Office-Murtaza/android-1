package com.app.belcobtm.presentation.core.formatter

import java.text.NumberFormat
import java.util.*

class DoubleCurrencyPriceFormatter(locale: Locale) : Formatter<Double> {

    private val numberFormat: NumberFormat = NumberFormat.getCurrencyInstance(locale)

    companion object {
        const val DOUBLE_CURRENCY_PRICE_FORMATTER_QUALIFIER = "DoubleCurrencyPriceFormatter"
    }

    override fun format(input: Double): String = numberFormat.format(input)
}