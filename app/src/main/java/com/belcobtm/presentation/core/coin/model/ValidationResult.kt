package com.belcobtm.presentation.core.coin.model

import androidx.annotation.StringRes

sealed class ValidationResult {

    object Valid : ValidationResult()

    class InValid(@StringRes val error: Int) : ValidationResult()
}