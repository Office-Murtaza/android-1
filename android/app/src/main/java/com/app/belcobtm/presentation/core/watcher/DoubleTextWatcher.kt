package com.app.belcobtm.presentation.core.watcher

import android.text.Editable
import android.text.TextWatcher

class DoubleTextWatcher(
    maxCharsAfterDotFirst: Int,
    maxCharsAfterDotSecond: Int,
    firstTextWatcher: (editable: Editable) -> Unit,
    secondTextWatcher: (editable: Editable) -> Unit
) {
    var isRunning = false
    var isDeleting = false

    val firstTextWatcher = object : TextWatcher {
        override fun onTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            isDeleting = count > after
        }

        override fun afterTextChanged(editable: Editable) {
            if (isRunning) return
            isRunning = true
            if (isValidBlock(editable, maxCharsAfterDotFirst)) {
                firstTextWatcher.invoke(editable)
            }
            isRunning = false
        }
    }

    val secondTextWatcher = object : TextWatcher {
        override fun onTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            isDeleting = count > after
        }

        override fun afterTextChanged(editable: Editable) {
            if (isRunning) return
            isRunning = true
            if (isValidBlock(editable, maxCharsAfterDotSecond)) {
                secondTextWatcher.invoke(editable)
            }
            isRunning = false
        }
    }

    companion object {
        const val MAX_CHARS_AFTER_DOT_CRYPTO = 6
        const val MAX_CHARS_AFTER_DOT_USD = 2
        private const val DOT_CHAR: Char = '.'

        fun isValidBlock(editable: Editable, maxCharsAfterDot: Int): Boolean = when {
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
                false
            }
            editable.isEmpty() -> false
            else -> {
                true
            }
        }
    }
}