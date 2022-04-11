package com.belcobtm.domain.settings.type

enum class VerificationIdentityFieldsType(val stringValue: String) {
    FIRST_NAME("firstGivenName"),
    LAST_NAME("firstSurName"),
    DAY_OF_BIRTH("dayOfBirth"),
    MONTH_OF_BIRTH("monthOfBirth"),
    YEAR_OF_BIRTH("yearOfBirth"),
    PROVINCE("stateProvinceCode"),
    CITY("city"),
    STREET_NAME("streetName"),
    STREET_TYPE("streetType"),
    BUILDING_NUMBER("buildingNumber"),
    ZIP_CODE("postalCode"),
    SSN("socialService"),
    NONE("none");

    companion object {
        fun fromString(string: String?): VerificationIdentityFieldsType =
            values().find { it.stringValue.compareTo(string ?: "none", ignoreCase = true) == 0 }
                ?: NONE
    }
}