package com.app.belcobtm.presentation.core.parser

interface StringParser<R> {

    fun parse(input: String): R
}