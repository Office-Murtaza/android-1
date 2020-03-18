package com.app.belcobtm.presentation.core.extensions

import android.widget.TextView

fun TextView.setTopDrawableWithText(resDrawable: Int) {
    setCompoundDrawablesWithIntrinsicBounds(0, resDrawable, 0, 0)
}

fun TextView.setDrawableStart(resDrawable: Int) = setCompoundDrawablesRelativeWithIntrinsicBounds(resDrawable, 0, 0, 0)