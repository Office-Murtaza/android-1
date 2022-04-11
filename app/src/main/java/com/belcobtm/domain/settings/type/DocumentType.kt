package com.belcobtm.domain.settings.type

enum class DocumentType(
    val stringValue: String,
    val shownStringValue: String,
    val needsBackPhoto: Boolean = true
) {
    DRIVING_LICENSE("DrivingLicence", "Driving License"),
    IDENTITY_CARD("IdentityCard", "Identity Card"),
    RESIDENCE_PERMIT("ResidencePermit", "Residence Permit"),
    PASSPORT("Passport", "Passport", false),
    VOTER_ID("VoterID", "Voter ID"),
    WORK_VISA("WorkVisa", "Work Visa", false),
    NONE("none", "none");

    companion object {
        fun fromString(string: String?): DocumentType =
            values().find { it.stringValue == string } ?: NONE

        fun fromShownString(string: String?): DocumentType =
            values().find { it.shownStringValue == string } ?: NONE
    }
}