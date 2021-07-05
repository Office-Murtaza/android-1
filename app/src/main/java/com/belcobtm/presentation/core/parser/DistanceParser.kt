package com.belcobtm.presentation.core.parser

class DistanceParser : StringParser<Int> {

    companion object {
        const val DISTANCE_INT_PARSER_QUALIFIER = "DistanceParser : StringParser"
    }

    override fun parse(input: String): Int = input.filter(Char::isDigit).ifEmpty { "0" }.toInt()
}