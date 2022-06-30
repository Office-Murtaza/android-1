package com.belcobtm.domain.settings

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.settings.item.VerificationBlankDataItem
import com.belcobtm.domain.settings.item.VerificationCountryDataItem
import com.belcobtm.domain.settings.item.VerificationDetailsDataItem
import com.belcobtm.domain.settings.item.VerificationDocumentDataItem
import com.belcobtm.domain.settings.item.VerificationDocumentFirebaseImages
import com.belcobtm.domain.settings.item.VerificationDocumentResponseDataItem
import com.belcobtm.domain.settings.item.VerificationFieldsDataItem
import com.belcobtm.domain.settings.item.VerificationIdentityDataItem
import com.belcobtm.domain.settings.item.VerificationIdentityResponseDataItem
import com.belcobtm.domain.settings.item.VerificationInfoDataItem
import com.belcobtm.domain.settings.item.VerificationVipDataItem

interface SettingsRepository {

    suspend fun getVerificationInfo(): Either<Failure, VerificationInfoDataItem>
    suspend fun getVerificationDetails(): Either<Failure, VerificationDetailsDataItem>
    suspend fun getVerificationFields(countryCode: String): Either<Failure, VerificationFieldsDataItem>
    suspend fun sendVerificationIdentity(identityDataItem: VerificationIdentityDataItem): Either<Failure, VerificationIdentityResponseDataItem>
    fun getVerificationCountries(): List<VerificationCountryDataItem>
    suspend fun sendVerificationDocument(
        documentDataItem: VerificationDocumentDataItem,
        firebaseImages: VerificationDocumentFirebaseImages
    ): Either<Failure, VerificationDocumentResponseDataItem>

    suspend fun sendVerificationBlank(blankDataItem: VerificationBlankDataItem, fileName: String): Either<Failure, Unit>
    suspend fun changePass(oldPassword: String, newPassword: String): Either<Failure, Boolean>
    suspend fun updatePhone(): Either<Failure, Boolean>
    suspend fun isPhoneUsed(phone: String): Either<Failure, Boolean>
    suspend fun setUserAllowedBioAuth(allowed: Boolean)
    suspend fun userAllowedBioAuth(): Either<Failure, Boolean>
    suspend fun bioAuthSupportedByPhone(): Either<Failure, Boolean>
    suspend fun needToShowRestrictions(): Either<Failure, Boolean>
    suspend fun setNeedToShowRestrictions(boolean: Boolean)
}
