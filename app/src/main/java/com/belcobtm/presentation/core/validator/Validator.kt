package com.belcobtm.presentation.core.validator

interface Validator<T> {
    fun isValid(input: T): Boolean
}