package com.belcobtm.presentation.core.parser

interface StringParser<R> {

    fun parse(input: String): R
}