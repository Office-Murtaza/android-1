package com.app.belcobtm.presentation.features.authorization.recover.seed

import android.content.Context
import android.text.Editable
import android.text.Spanned
import android.text.TextWatcher
import androidx.core.text.getSpans

class RecoverSeedWatcher(private val context: Context) : TextWatcher {
    private var start: Int = 0
    private var end: Int = 0
    private var before: Int = 0
    private var count: Int = 0

    override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) = Unit

    override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
        this.start = start
        this.end = start + count
        this.before = before
        this.count = count
    }

    override fun afterTextChanged(editable: Editable) {
        val wordList: List<String> = editable.toString()
            .replace(CHAR_NEXT_LINE, CHAR_SPACE)
            .splitToSequence(CHAR_SPACE)
            .filter { it.isNotEmpty() }
            .toList()

        if (editable.isNotEmpty() && wordList.isNotEmpty()) {
            val isInsertedPhrase = before == 0 && count > 1
            val lastChar = editable.subSequence(if (end > 0) end - 1 else 0, end).toString()
            val isRemoving = count < before
            when {
                isRemoving && lastChar != CHAR_SPACE && lastChar != CHAR_NEXT_LINE ->
                    editable.getSpans<ChipSpan>(start, end).forEach { editable.removeSpan(it) }
                !isRemoving && (lastChar == CHAR_SPACE || lastChar == CHAR_NEXT_LINE) -> {
                    var endIndex = 0
                    wordList.forEach { word ->
                        val startIndex = editable.indexOf(word, endIndex, true)
                        endIndex = startIndex + word.length
                        editable.setSpan(
                            ChipSpan(
                                context,
                                word,
                                null,
                                null
                            ),
                            startIndex,
                            endIndex,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                }
                isInsertedPhrase -> {
                    val spanList = editable.getSpans<ChipSpan>(0, editable.lastIndex)
                    if (spanList.isEmpty()) {
                        setSpanForAll(editable, wordList)
                        if (end == editable.length) {
                            editable.insert(end, CHAR_SPACE)
                        }
                    } else {
                        spanList.forEach { editable.removeSpan(it) }
                        setSpanForAll(editable, wordList)
                        if (end == editable.length) {
                            editable.insert(end, CHAR_SPACE)
                        }
                    }
                }
            }
        }
    }

    private fun setSpanForAll(editable: Editable, list: List<String>) {
        var endIndex = 0
        list.forEach { word ->
            val startIndex = editable.indexOf(word, endIndex, true)
            endIndex = startIndex + word.length
            editable.setSpan(
                ChipSpan(
                    context,
                    word,
                    null,
                    null
                ),
                startIndex,
                endIndex,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    companion object {
        const val CHAR_NEXT_LINE: String = "\n"
        const val CHAR_SPACE: String = " "
    }
}