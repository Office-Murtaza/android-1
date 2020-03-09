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

fun TextInputLayout.setText(text: String) = editText?.setText(text)

fun TextInputLayout.clear() = editText?.setText("")

fun TextInputLayout.isNotBlank() = editText?.getString()?.isNotBlank() ?: false

fun TextInputLayout.actionDoneListener(listener: () -> Unit) {
    editText?.setOnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            listener.invoke()
        }
        false
    }
}