package com.belcobtm.presentation.tools.formatter

interface Formatter<T> {

    fun format(input: T): String
}