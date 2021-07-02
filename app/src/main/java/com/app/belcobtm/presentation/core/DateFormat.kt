package com.app.belcobtm.presentation.core

import java.text.SimpleDateFormat
import java.util.*

object DateFormat {
    private const val DATE_FORMAT_SHORT = "MM/dd/yyyy"
    private const val DATE_FORMAT_LONG = "MM/dd/yyyy, hh:mm a"
    const val CHAT_DATE_FORMAT = "hh:mm a MM/dd/yyyy"

    val sdfShort: SimpleDateFormat by lazy {
        SimpleDateFormat(DATE_FORMAT_SHORT, Locale.US)
    }
    val sdfLong: SimpleDateFormat by lazy {
        SimpleDateFormat(DATE_FORMAT_LONG, Locale.US)
    }
}
