package com.belcobtm.presentation.tools.extensions

import android.os.Build
import android.text.Html
import android.text.Spanned
import java.util.regex.Pattern

fun String.toHtmlSpan(): Spanned = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
    Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
} else {
    @Suppress("DEPRECATION")
    Html.fromHtml(this)
}

private val EMAIL_RFC_3696_ADDRESS_PATTERN =
    Pattern.compile("[A-Z0-9a-z#$%&'*+–/=?^_`.{|}~-]{1,256}@[A-Za-z0-9.-]{1,64}\\.[A-Za-z]{2,25}")

/*
Note - the default android email pattern doesn't support RFC 3696 standard and doesn't allow next symbols:
    ! # $ % & ' * + – / = ? ^ ` { | } ~
     Official RFC5322 regex with Caps -> "(?:[A-Za-z0-9!#\$%&'*+/=?^_`{|}~-]+(?:\\.[A-Za-z0-9!#\$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?\\.)+[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[A-Za-z0-9-]*[A-Za-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])"
*/
fun CharSequence.isEmail(): Boolean = EMAIL_RFC_3696_ADDRESS_PATTERN.matcher(this).matches()

fun String.getPhoneForRequest(): String = this.replace("[-() ]".toRegex(), "")
