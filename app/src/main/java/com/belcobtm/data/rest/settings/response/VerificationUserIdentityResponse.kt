package com.belcobtm.data.rest.settings.response

import com.belcobtm.data.rest.settings.request.CommunicationInfo
import com.belcobtm.data.rest.settings.request.LocationInfo
import com.belcobtm.data.rest.settings.request.NationalId
import com.belcobtm.data.rest.settings.request.PersonInfo

data class VerificationUserIdentityResponse(
    val identityVerification: VerificationIdentity
)

data class VerificationIdentity(
    val request: VerificationIdentityRequest?,
    val response: VerificationIdentityResponse?,
)

data class VerificationIdentityRequest(
    val personInfo: PersonInfo,
    val location: LocationInfo,
    val communication: CommunicationInfo,
    val nationalIds: List<NationalId>
)

data class VerificationIdentityResponse(
    val transactionID: String?,
    val countryCode: String?,
    val record: RecordIdentityResponse
)

data class RecordIdentityResponse(
    val recordStatus: String?,
    val datasourceResults: List<DataSourceIdentityResult>
)

data class DataSourceIdentityResult(
    val errors: List<DataSourceError>,
    val datasourceFields: List<DataSourceIdentityFields>
)

data class DataSourceIdentityFields(
    val fieldName: String?,
    val status: String?
)

data class DataSourceError(
    val code: String?,
    val message: String?,
)