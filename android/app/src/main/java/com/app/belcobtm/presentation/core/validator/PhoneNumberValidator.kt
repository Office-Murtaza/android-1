package com.app.belcobtm.presentation.core.validator

import io.michaelrocks.libphonenumber.android.NumberParseException
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil

class PhoneNumberValidator(
    private val phoneNumberUtils: PhoneNumberUtil
) : Validator<String> {

    override fun isValid(input: String): Boolean =
        if (input.isNotBlank()) {
            try {
                val number = phoneNumberUtils.parse(input, "")
                phoneNumberUtils.isValidNumber(number)
            } catch (e: NumberParseException) {
                false
            }
        } else {
            false
        }
}