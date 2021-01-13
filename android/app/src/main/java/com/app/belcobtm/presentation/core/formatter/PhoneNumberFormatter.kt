package com.app.belcobtm.presentation.core.formatter

import android.telephony.PhoneNumberUtils

class PhoneNumberFormatter(private val countryCode: String) : Formatter<String> {

    override fun format(input: String): String =
        PhoneNumberUtils.formatNumber(input, countryCode) ?: input
}