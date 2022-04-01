package com.belcobtm.domain.settings

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.settings.item.*

interface SettingsRepository {
    suspend fun getVerificationInfo(): Either<Failure, VerificationInfoDataItem>
    suspend fun getVerificationDetails():Either<Failure, VerificationDetailsDataItem>
    suspend fun getVerificationFields(countryCode : String): Either<Failure, VerificationFieldsDataItem>
    suspend fun sendVerificationIdentity(identityDataItem : VerificationIdentityDataItem): Either<Failure, VerificationIdentityResponseDataItem>
    suspend fun sendVerificationDocument(documentDataItem : VerificationDocumentDataItem,firebaseImages: VerificationDocumentFirebaseImages): Either<Failure, VerificationDocumentResponseDataItem>
    suspend fun sendVerificationBlank(blankDataItem: VerificationBlankDataItem, fileName: String): Either<Failure, Unit>
    fun getVerificationCountries(): List<VerificationCountryDataItem>
    suspend fun sendVerificationVip(vipDataItem: VerificationVipDataItem, fileName: String): Either<Failure, Unit>
    suspend fun getPhone(): Either<Failure, String>
    suspend fun changePass(oldPassword: String, newPassword: String): Either<Failure, Boolean>
    suspend fun updatePhone(phone: String): Either<Failure, Boolean>
    suspend fun verifyPhone(phone: String): Either<Failure, Boolean>
    suspend fun setUserAllowedBioAuth(allowed: Boolean)
    suspend fun userAllowedBioAuth(): Either<Failure, Boolean>
    suspend fun bioAuthSupportedByPhone(): Either<Failure, Boolean>
    suspend fun needToShowRestrictions(): Either<Failure, Boolean>
    suspend fun setNeedToShowRestrictions(boolean: Boolean)
}