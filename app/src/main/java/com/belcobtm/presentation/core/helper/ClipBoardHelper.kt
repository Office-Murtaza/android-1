package com.belcobtm.presentation.core.helper

import android.content.ClipboardManager
import android.content.Context

class ClipBoardHelper(context: Context) {

    private val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    fun getTextFromClipboard(): String? {
        val clipData = clipboard.primaryClip
        val item = clipData?.getItemAt(0)
        return item?.text?.toString()
    }
}