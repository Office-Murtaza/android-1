package com.belcobtm.presentation.tools.extensions

import android.view.View

fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.toggle(isVisible: Boolean) = if (isVisible) show() else hide()