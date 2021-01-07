package com.app.belcobtm.presentation.core.validator

interface Validator<T> {
    fun isValid(input: T): Boolean
}