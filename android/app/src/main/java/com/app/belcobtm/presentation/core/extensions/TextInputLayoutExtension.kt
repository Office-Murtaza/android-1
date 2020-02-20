package com.app.belcobtm.presentation.core.extensions

import com.google.android.material.textfield.TextInputLayout

fun TextInputLayout.showError(resText: Int?) = if (resText == null) {
    error = null
} else {
    error = resources.getString(resText)
}

fun TextInputLayout.clearError() {
    error = null
}

fun TextInputLayout.getString(): String = editText?.text?.toString() ?: ""

fun TextInputLayout.setText(text: String) = editText?.setText(text)