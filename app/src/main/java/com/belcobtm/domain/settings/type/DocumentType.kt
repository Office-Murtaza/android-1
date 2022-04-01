package com.belcobtm.domain.settings.type

enum class DocumentType(val stringValue: String) {
    DRIVING_LICENSE("DrivingLicence"),
    IDENTITY_CARD("IdentityCard"),
    RESIDENCE_PERMIT("ResidencePermit"),
    PASSPORT("Passport"),
    VOTER_ID("VoterID"),
    WORK_VISA("WorkVisa"),
    NONE("none");

    companion object {
        fun fromString(string: String?): DocumentType =
            values().find { it.stringValue == string } ?: NONE
    }
}