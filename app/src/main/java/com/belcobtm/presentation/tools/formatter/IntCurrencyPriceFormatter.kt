package com.belcobtm.presentation.tools.formatter

import java.text.NumberFormat
import java.util.Locale

class IntCurrencyPriceFormatter(locale: Locale) : Formatter<Int> {

    private val numberFormat: NumberFormat = NumberFormat.getCurrencyInstance(locale)

    init {
        numberFormat.maximumFractionDigits = 0
        numberFormat.isParseIntegerOnly = true
    }

    companion object {

        const val INT_CURRENCY_PRICE_FORMATTER_QUALIFIER = "IntCurrencyPriceFormatter"
    }

    override fun format(input: Int): String = numberFormat.format(input)

}
