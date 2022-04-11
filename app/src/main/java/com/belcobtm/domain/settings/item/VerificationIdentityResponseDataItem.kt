package com.belcobtm.domain.settings.item

import com.belcobtm.domain.settings.type.RecordStatus

data class VerificationIdentityResponseDataItem(
    val recordStatus: RecordStatus,
    val firstNameValue: String,
    val lastNameValue: String,
    val dayOfBirthValue: Int,
    val monthOfBirthValue: Int,
    val yearOfBirthValue: Int,
    val provinceValue: String,
    val cityValue: String,
    val streetNameValue: String,
    val buildingNumberValue: String,
    val zipCodeValue: String,
    val ssnValue: String,
    val firstNameValidationError: Boolean,
    val lastNameValidationError: Boolean,
    val birthDateValidationError: Boolean,
    val provinceValidationError: Boolean,
    val cityValidationError: Boolean,
    val streetNameValidationError: Boolean,
    val buildingNumberValidationError: Boolean,
    val zipCodeValidationError: Boolean,
    val ssnValidationError: Boolean,
    )