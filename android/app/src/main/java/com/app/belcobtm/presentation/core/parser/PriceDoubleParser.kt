package com.app.belcobtm.presentation.core.parser

import java.util.*

class PriceDoubleParser(locale: Locale) : StringParser<Double> {

    private val currency = Currency.getInstance(locale)

    override fun parse(input: String): Double {
        val cleanString = input.replace("[${currency.symbol},.]".toRegex(), "").ifEmpty { "0" }
        return cleanString.toDouble()
    }
}