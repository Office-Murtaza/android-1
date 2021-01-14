package com.app.belcobtm.presentation.core.views.listeners

import android.text.Editable
import android.text.TextWatcher
import com.app.belcobtm.presentation.core.watcher.DoubleTextWatcher.Companion.MAX_CHARS_AFTER_DOT_CRYPTO

class SafeDecimalEditTextWatcher(
    private val maxCharsAfterDot: Int = MAX_CHARS_AFTER_DOT_CRYPTO,
    private val listener: (Editable) -> Unit
) : TextWatcher {

    companion object {
        private const val DOT_CHAR: Char = '.'
    }

    override fun afterTextChanged(editable: Editable) {
        if (isValidBlock(editable, maxCharsAfterDot)) {
            listener(editable)
        }
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit

    private fun isValidBlock(editable: Editable, maxCharsAfterDot: Int): Boolean = when {
        editable.isNotEmpty() && editable.first() == DOT_CHAR -> {
            editable.clear()
            false
        }
        editable.isNotEmpty() && editable.last() == DOT_CHAR && editable.count { it == DOT_CHAR } > 1 -> {
            editable.delete(editable.lastIndex, editable.length)
            false
        }
        editable.contains(DOT_CHAR) && (editable.lastIndex - editable.indexOf(DOT_CHAR)) > maxCharsAfterDot -> {
            editable.delete(editable.lastIndex - 1, editable.lastIndex)
            true
        }
        else -> {
            true
        }
    }

}