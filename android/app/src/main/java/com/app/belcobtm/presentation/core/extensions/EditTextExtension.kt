package com.app.belcobtm.presentation.core.extensions

import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.widget.EditText

fun EditText.getString(): String = text.toString()

fun EditText.afterTextChanged(afterTextChanged: (Editable) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit

        override fun afterTextChanged(editable: Editable) {
            afterTextChanged.invoke(editable)
        }
    })
}

fun EditText.onTextChanged(textChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            textChanged.invoke(p0.toString())
        }

        override fun afterTextChanged(editable: Editable?) = Unit
    })
}

fun EditText.actionDoneListener(listener: () -> Unit) {
    setOnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            listener.invoke()
        }
        false
    }
}