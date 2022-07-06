package com.belcobtm.presentation.tools.validator

interface Validator<T> {
    fun isValid(input: T): Boolean
}