package com.belcobtm.data

import android.app.Application
import androidx.biometric.BiometricManager
import com.belcobtm.data.disk.AssetsDataStore
import com.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.belcobtm.data.rest.settings.SettingsApiService
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.settings.SettingsRepository
import com.belcobtm.domain.settings.item.*
import com.belcobtm.domain.settings.type.RecordStatus
import com.belcobtm.domain.settings.type.VerificationStatus

class SettingsRepositoryImpl(
    private val application: Application,
    private val apiService: SettingsApiService,
    private val assetsDataStore: AssetsDataStore,
    private val prefHelper: SharedPreferencesHelper
) : SettingsRepository {

    override suspend fun getVerificationDetails(): Either<Failure, VerificationDetailsDataItem> {
        val response = apiService.getVerificationDetails(prefHelper.userId)
        return if (response.isRight) {
            val responseItem = (response as Either.Right).b

            val identityVerificationResponse =
                VerificationIdentityResponseDataItem(
                    recordStatus = RecordStatus.fromString(
                        responseItem.identityVerification?.record?.recordStatus
                    )
                )

            Either.Right(
                VerificationDetailsDataItem(
                    identityVerification = identityVerificationResponse,
                    documentVerification = responseItem.documentVerification,
                    documentVerificationComplete = responseItem.documentVerificationComplete,
                    supportedCountries = responseItem.supportedCountries,
                    sdkToken = responseItem.sdkToken,
                )
            )
        } else {
            response as Either.Left
        }
    }

    override suspend fun getVerificationFields(countryCode: String): Either<Failure, VerificationFieldsDataItem> {
        val response = apiService.getVerificationFields(countryCode)
        return if (response.isRight) {
            val responseItem = (response as Either.Right).b
            Either.Right(
                VerificationFieldsDataItem(
                    "xx"
                )
            )
        } else {
            response as Either.Left
        }
    }

    override suspend fun sendVerificationIdentity(identityDataItem: VerificationIdentityDataItem): Either<Failure, VerificationIdentityResponseDataItem> {
        val response = apiService.sendVerificationIdentity(prefHelper.userId, identityDataItem)
        return if (response.isRight) {
            val responseItem = (response as Either.Right).b
            Either.Right(
                VerificationIdentityResponseDataItem(
                    recordStatus = RecordStatus.fromString(responseItem.countryCode)
                )
            )
        } else {
            response as Either.Left
        }
    }

    override suspend fun sendVerificationDocument(
        documentDataItem: VerificationDocumentDataItem,
        firebaseImages: VerificationDocumentFirebaseImages
    ): Either<Failure, VerificationDocumentResponseDataItem> {
        val response =
            apiService.sendVerificationDocument(prefHelper.userId, documentDataItem, firebaseImages)
        return if (response.isRight) {
            val responseItem = (response as Either.Right).b
            Either.Right(
                VerificationDocumentResponseDataItem(
                    response = "Success"
                )
            )
        } else {
            response as Either.Left
        }
    }

    override suspend fun getVerificationInfo(): Either<Failure, VerificationInfoDataItem> {
        val response = apiService.getVerificationInfo(prefHelper.userId)
        return if (response.isRight) {
            val responseItem = (response as Either.Right).b
            Either.Right(
                VerificationInfoDataItem(
                    id = responseItem.id,
                    status = VerificationStatus.fromString(responseItem.status),
                    txLimit = responseItem.txLimit,
                    dayLimit = responseItem.dailyLimit,
                    message = responseItem.message.orEmpty(),
                    idCardNumber = responseItem.idCardNumber.orEmpty(),
                    idCardNumberFilename = responseItem.idCardNumberFilename.orEmpty(),
                    firstName = responseItem.firstName.orEmpty(),
                    lastName = responseItem.lastName.orEmpty(),
                    address = responseItem.address.orEmpty(),
                    province = responseItem.province.orEmpty(),
                    country = responseItem.country.orEmpty(),
                    zipCode = responseItem.zipCode.orEmpty(),
                    city = responseItem.city.orEmpty(),
                )
            )
        } else {
            response as Either.Left
        }
    }

    override suspend fun sendVerificationBlank(
        blankDataItem: VerificationBlankDataItem,
        fileName: String
    ): Either<Failure, Unit> = apiService.sendVerificationBlank(
        prefHelper.userId, blankDataItem, fileName
    )

    override fun getVerificationCountries(): List<VerificationCountryDataItem> =
        assetsDataStore.getCountries()

    override suspend fun sendVerificationVip(
        vipDataItem: VerificationVipDataItem,
        fileName: String
    ): Either<Failure, Unit> = apiService.sendVerificationVip(
        prefHelper.userId, vipDataItem, fileName
    )

    override suspend fun changePass(
        oldPassword: String,
        newPassword: String
    ): Either<Failure, Boolean> = apiService.changePass(prefHelper.userId, oldPassword, newPassword)

    override suspend fun getPhone(): Either<Failure, String> =
        apiService.getPhone(prefHelper.userId)

    override suspend fun updatePhone(phone: String): Either<Failure, Boolean> =
        apiService.updatePhone(prefHelper.userId, phone)

    override suspend fun verifyPhone(phone: String): Either<Failure, Boolean> =
        apiService.verifyPhone(prefHelper.userId, phone)

    override suspend fun setUserAllowedBioAuth(allowed: Boolean) {
        prefHelper.userAllowedBioAuth = allowed
    }

    override suspend fun bioAuthSupportedByPhone(): Either<Failure, Boolean> {
        val bioManager = BiometricManager.from(application.applicationContext)
        val error = Either.Left(Failure.MessageError("BIOMETRIC_NOT_SUPPORTED"))
        return when (bioManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> Either.Right(true)
            else -> error
        }
    }

    override suspend fun needToShowRestrictions(): Either<Failure, Boolean> {
        return Either.Right(prefHelper.needToShowRestrictions)
    }

    override suspend fun setNeedToShowRestrictions(boolean: Boolean) {
        prefHelper.needToShowRestrictions = boolean
    }

    override suspend fun userAllowedBioAuth(): Either<Failure, Boolean> {
        return Either.Right(prefHelper.userAllowedBioAuth)
    }
}
