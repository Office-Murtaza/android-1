package com.app.belcobtm.presentation.core.watcher

import android.text.Editable
import com.app.belcobtm.presentation.core.views.listeners.SafeDecimalEditTextWatcher

class DoubleTextWatcher(
    maxCharsAfterDotFirst: Int = MAX_CHARS_AFTER_DOT_CRYPTO,
    maxCharsAfterDotSecond: Int = MAX_CHARS_AFTER_DOT_USD,
    firstTextWatcher: (editable: Editable) -> Unit,
    secondTextWatcher: (editable: Editable) -> Unit = {}
) {
    var isRunning = false

    val firstTextWatcher = SafeDecimalEditTextWatcher(maxCharsAfterDotFirst) { editable ->
        if (isRunning) return@SafeDecimalEditTextWatcher
        isRunning = true
        firstTextWatcher.invoke(editable)
        isRunning = false
    }

    val secondTextWatcher = SafeDecimalEditTextWatcher(maxCharsAfterDotSecond) { editable ->
        if (isRunning) return@SafeDecimalEditTextWatcher
        isRunning = true
        secondTextWatcher.invoke(editable)
        isRunning = false
    }

    companion object {
        const val MAX_CHARS_AFTER_DOT_CRYPTO = 6
        const val MAX_CHARS_AFTER_DOT_USD = 2
    }
}