package com.belcobtm.presentation.core

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateFormat {

    private const val DATE_FORMAT_SHORT = "dd MMM yyyy"
    private const val DATE_FORMAT_LONG = "dd MMM yyyy, hh:mm a"
    const val CHAT_DATE_FORMAT = "dd MMM yyyy, "

    fun getUserSelectedTimeFormat(context: Context, date: Date): String {
        val dateFormat = android.text.format.DateFormat.getTimeFormat(context)
        return dateFormat.format(date)
    }

    val sdfShort: SimpleDateFormat by lazy {
        SimpleDateFormat(DATE_FORMAT_SHORT, Locale.US)
    }
    val sdfLong: SimpleDateFormat by lazy {
        SimpleDateFormat(DATE_FORMAT_LONG, Locale.US)
    }

}
