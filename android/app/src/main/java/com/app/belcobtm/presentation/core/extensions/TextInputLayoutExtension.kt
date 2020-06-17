package com.app.belcobtm.presentation.core.extensions

import android.view.inputmethod.EditorInfo
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

fun TextInputLayout.getDouble(): Double = when {
    (editText?.text?.isEmpty() == true)
            || (editText?.text?.toString()?.replace("[^\\d]", "")?.length == 0) -> 0.0
    else -> {
        editText?.text?.toString()?.toDouble() ?: 0.0
    }
}

fun TextInputLayout.setText(text: String) = editText?.setText(text)

fun TextInputLayout.clearText() = editText?.setText("")

fun TextInputLayout.isNotBlank() = editText?.getString()?.isNotBlank() ?: false

fun TextInputLayout.actionDoneListener(listener: () -> Unit) {
    editText?.setOnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            listener.invoke()
        }
        false
    }
}

fun TextInputLayout.setDrawableStartEnd(resDrawableStart: Int, resDrawableEnd: Int) =
    editText?.setCompoundDrawablesRelativeWithIntrinsicBounds(resDrawableStart, 0, resDrawableEnd, 0)