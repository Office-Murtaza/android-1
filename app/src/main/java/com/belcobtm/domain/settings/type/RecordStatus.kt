package com.belcobtm.domain.settings.type

enum class RecordStatus(val stringValue: String) {
    MATCH("match"),
    NO_MATCH("noMatch");

    companion object {
        fun fromString(string: String?): RecordStatus =
            values().find { it.stringValue == string } ?: NO_MATCH
    }
}