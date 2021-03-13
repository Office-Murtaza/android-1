package com.app.belcobtm.presentation.core.provider.string

import androidx.annotation.StringRes

interface StringProvider {

    fun getString(@StringRes res: Int): String

    fun getString(@StringRes res: Int, vararg args: Any?): String
}