package com.belcobtm.presentation.tools.formatter

import java.text.NumberFormat
import java.util.Locale

class CryptoPriceFormatter(locale: Locale) : Formatter<Double> {

    private val numberFormat: NumberFormat = NumberFormat.getNumberInstance(locale)

    override fun format(input: Double): String = "$" + numberFormat.format(input)
        .replace("[.]000".toRegex(), "")
        .removeSuffix("0")
        .removeSuffix("0")
        .removeSuffix(".0")

    companion object {

        const val CRYPTO_PRICE_FORMATTER_QUALIFIER = "CryptoPriceFormatter"
    }

}
