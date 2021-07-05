package com.belcobtm.presentation.core.parser

class PriceDoubleParser : StringParser<Double> {

    companion object {
        const val PRICE_DOUBLE_PARSER_QUALIFIER = "PriceDoubleParser"
    }

    override fun parse(input: String): Double =
        input.toDouble()
}