package com.belcobtm.presentation.core.formatter

interface Formatter<T> {

    fun format(input: T): String
}