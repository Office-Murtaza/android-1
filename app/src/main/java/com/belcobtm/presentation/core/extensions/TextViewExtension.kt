package com.belcobtm.presentation.core.extensions

import android.graphics.drawable.Drawable
import android.widget.TextView

fun TextView.setTopDrawableWithText(resDrawable: Int) {
    setCompoundDrawablesWithIntrinsicBounds(0, resDrawable, 0, 0)
}

fun TextView.setDrawableStart(resDrawable: Int) = setCompoundDrawablesRelativeWithIntrinsicBounds(resDrawable, 0, 0, 0)

fun TextView.setDrawableTop(resDrawable: Int) = setCompoundDrawablesRelativeWithIntrinsicBounds(0, resDrawable, 0, 0)

fun TextView.setDrawableEnd(resDrawable: Int) = setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, resDrawable, 0)

fun TextView.setDrawableEnd(drawable: Drawable) = setCompoundDrawablesRelativeWithIntrinsicBounds(
    compoundDrawables[0],
    compoundDrawables[1],
    drawable,
    compoundDrawables[3]
)

fun TextView.cleanDrawables() = setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
