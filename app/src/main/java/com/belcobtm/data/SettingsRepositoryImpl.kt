package com.belcobtm.data

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.biometric.BiometricManager
import com.belcobtm.data.cloud.storage.CloudStorage
import com.belcobtm.data.disk.AssetsDataStore
import com.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.belcobtm.data.rest.settings.SettingsApiService
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.settings.SettingsRepository
import com.belcobtm.domain.settings.item.*
import com.belcobtm.domain.settings.type.*
import java.net.URL

class SettingsRepositoryImpl(
    private val application: Application,
    private val apiService: SettingsApiService,
    private val assetsDataStore: AssetsDataStore,
    private val prefHelper: SharedPreferencesHelper,
    private val cloudStorage: CloudStorage
) : SettingsRepository {

    override suspend fun getVerificationDetails(): Either<Failure, VerificationDetailsDataItem> {
        val response = apiService.getVerificationDetails(prefHelper.userId)
        return if (response.isRight) {
            val responseItem = (response as Either.Right).b
            var identityHasErrors = false
            var identityVerificationResponse: VerificationIdentityResponseDataItem? = null
            var documentVerificationResponse: VerificationDocumentResponseDataItem? = null
            responseItem.userVerification?.identityVerification?.let {
                val mapIdentityFieldsError =
                    mutableMapOf<VerificationIdentityFieldsType, Boolean>()
                if (RecordStatus.fromString(it.response?.record?.recordStatus) == RecordStatus.NO_MATCH) {
                    it.response?.record?.datasourceResults?.let { listOfDataSource ->
                        for (dataSource in listOfDataSource) {
                            for (error in dataSource.errors) {
                                val fieldName = error.message?.substringAfterLast(":")?.trim()
                                mapIdentityFieldsError[VerificationIdentityFieldsType.fromString(
                                    fieldName
                                )] = true
                                identityHasErrors = true
                            }
                        }
                    }
                }
                if (RecordStatus.fromString(it.response?.record?.recordStatus) == RecordStatus.NO_MATCH && !identityHasErrors) {
                    it.response?.record?.datasourceResults?.let { listOfDataSource ->
                        for (dataSource in listOfDataSource) {
                            for (field in dataSource.datasourceFields) {
                                if (RecordStatus.fromString(field.status) != RecordStatus.MATCH) {
                                    val fieldName = field.fieldName
                                    mapIdentityFieldsError[VerificationIdentityFieldsType.fromString(
                                        fieldName
                                    )] = true
                                }
                            }
                        }
                    }
                }

                val verificationData = it.request
                verificationData?.let { verificationData ->
                    identityVerificationResponse = VerificationIdentityResponseDataItem(
                        recordStatus = RecordStatus.fromString(it.response?.record?.recordStatus),
                        firstNameValue = verificationData.personInfo.firstGivenName,
                        lastNameValue = verificationData.personInfo.firstSurName,
                        dayOfBirthValue = verificationData.personInfo.dayOfBirth,
                        monthOfBirthValue = verificationData.personInfo.monthOfBirth,
                        yearOfBirthValue = verificationData.personInfo.yearOfBirth,
                        buildingNumberValue = verificationData.location.buildingNumber,
                        streetNameValue = verificationData.location.streetName,
                        cityValue = verificationData.location.city,
                        provinceValue = verificationData.location.stateProvinceCode,
                        zipCodeValue = verificationData.location.postalCode,
                        ssnValue = verificationData.nationalIds.first().number,
                        sourceOfFunds = responseItem.userVerification.sourceOfFunds ?: "",
                        occupation = responseItem.userVerification.occupation ?: "",
                        firstNameValidationError = mapIdentityFieldsError.get(
                            VerificationIdentityFieldsType.FIRST_NAME
                        ) ?: false,
                        lastNameValidationError = mapIdentityFieldsError.get(
                            VerificationIdentityFieldsType.LAST_NAME
                        ) ?: false,
                        birthDateValidationError = mapIdentityFieldsError.get(
                            VerificationIdentityFieldsType.DAY_OF_BIRTH
                        ) ?: false || mapIdentityFieldsError.get(VerificationIdentityFieldsType.MONTH_OF_BIRTH) ?: false || mapIdentityFieldsError.get(
                            VerificationIdentityFieldsType.YEAR_OF_BIRTH
                        ) ?: false,
                        provinceValidationError = mapIdentityFieldsError.get(
                            VerificationIdentityFieldsType.PROVINCE
                        ) ?: false,
                        cityValidationError = mapIdentityFieldsError.get(
                            VerificationIdentityFieldsType.CITY
                        )
                            ?: false,
                        streetNameValidationError = mapIdentityFieldsError.get(
                            VerificationIdentityFieldsType.STREET_NAME
                        ) ?: false,
                        buildingNumberValidationError = mapIdentityFieldsError.get(
                            VerificationIdentityFieldsType.BUILDING_NUMBER
                        ) ?: false,
                        zipCodeValidationError = mapIdentityFieldsError.get(
                            VerificationIdentityFieldsType.ZIP_CODE
                        ) ?: false,
                        ssnValidationError = mapIdentityFieldsError.get(
                            VerificationIdentityFieldsType.SSN
                        )
                            ?: false,
                    )
                }
            }

            var documentsHasErrors = false
            var documentsOverrideInvalidFields = false
            responseItem.userVerification?.documentVerification?.let {
                val mapDocumentFieldsError =
                    mutableMapOf<VerificationDocumentFieldsType, Boolean>()
                if (RecordStatus.fromString(it.response?.record?.recordStatus) == RecordStatus.NO_MATCH) {
                    it.response?.record?.datasourceResults?.let { listOfDataSource ->
                        for (dataSource in listOfDataSource) {
                            for (error in dataSource.errors) {
                                val fieldName = error.message?.substringAfterLast(":")?.trim()
                                mapDocumentFieldsError[VerificationDocumentFieldsType.fromString(
                                    fieldName
                                )] = true
                                documentsHasErrors = true
                            }
                        }
                    }
                }
                if (RecordStatus.fromString(it.response?.record?.recordStatus) == RecordStatus.NO_MATCH && !documentsHasErrors) {
                    documentsOverrideInvalidFields = true
                }

                it.request?.let { request ->
                    var frontImageBitmap: Bitmap? = null
                    var backImageBitmap: Bitmap? = null
                    var selfieImageBitmap: Bitmap? = null
                    try {
                        frontImageBitmap =
                            BitmapFactory.decodeStream(
                                URL(
                                    cloudStorage.getLink(
                                        request.firebase?.frontImage ?: ""
                                    )
                                ).openStream()
                            )
                        selfieImageBitmap =
                            BitmapFactory.decodeStream(
                                URL(
                                    cloudStorage.getLink(
                                        request.firebase?.selfieImage ?: ""
                                    )
                                ).openStream()
                            )
                        backImageBitmap =
                            BitmapFactory.decodeStream(
                                URL(
                                    cloudStorage.getLink(
                                        request.firebase?.backImage ?: ""
                                    )
                                ).openStream()
                            )
                    } catch (e: Exception) {

                    }

                    documentVerificationResponse = VerificationDocumentResponseDataItem(
                        recordStatus = RecordStatus.fromString(it.response?.record?.recordStatus),
                        documentType = DocumentType.fromString(request.documentType),
                        transactionId = it.response?.transactionID,
                        frontImageBitmap = frontImageBitmap,
                        backImageBitmap = backImageBitmap,
                        selfieImageBitmap = selfieImageBitmap,
                        frontImageValidationError = mapDocumentFieldsError.get(
                            VerificationDocumentFieldsType.FRONT_SCAN
                        ) ?: documentsOverrideInvalidFields,
                        backImageValidationError = mapDocumentFieldsError.get(
                            VerificationDocumentFieldsType.BACK_SCAN
                        ) ?: documentsOverrideInvalidFields,
                        selfieImageValidationError = mapDocumentFieldsError.get(
                            VerificationDocumentFieldsType.SELFIE
                        ) ?: documentsOverrideInvalidFields,
                    )
                }
            }

            Either.Right(
                VerificationDetailsDataItem(
                    identityVerification = identityVerificationResponse,
                    documentVerification = documentVerificationResponse,
                    supportedCountries = responseItem.supportedCountries,
                    sdkToken = responseItem.sdkToken,
                    selectedCountry = responseItem.supportedCountries.firstOrNull {
                        it.code == responseItem.userVerification?.countryCode
                    }
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
        val response = apiService.sendVerificationIdentity(
            prefHelper.userId,
            prefHelper.userPhone,
            identityDataItem
        )
        return if (response.isRight) {
            var identityHasErrors = false
            val responseItem = (response as Either.Right).b
            val mapIdentityFieldsError = mutableMapOf<VerificationIdentityFieldsType, Boolean>()
            responseItem.identityVerification.response?.record?.datasourceResults?.let { listOfDataSource ->
                for (dataSource in listOfDataSource) {
                    for (error in dataSource.errors) {
                        val fieldName = error.message?.substringAfterLast(":")?.trim()
                        mapIdentityFieldsError[VerificationIdentityFieldsType.fromString(fieldName)] =
                            true
                        identityHasErrors = true
                    }
                }
            }
            if (RecordStatus.fromString(responseItem.identityVerification.response?.record?.recordStatus) == RecordStatus.NO_MATCH && !identityHasErrors) {
                responseItem.identityVerification.response?.record?.datasourceResults?.let { listOfDataSource ->
                    for (dataSource in listOfDataSource) {
                        for (field in dataSource.datasourceFields) {
                            if (RecordStatus.fromString(field.status) != RecordStatus.MATCH) {
                                val fieldName = field.fieldName
                                mapIdentityFieldsError[VerificationIdentityFieldsType.fromString(
                                    fieldName
                                )] = true
                            }
                        }
                    }
                }
            }

            val verificationData = responseItem.identityVerification.request!!
            Either.Right(
                VerificationIdentityResponseDataItem(
                    recordStatus = RecordStatus.fromString(responseItem.identityVerification.response?.record?.recordStatus),
                    firstNameValue = verificationData.personInfo.firstGivenName,
                    lastNameValue = verificationData.personInfo.firstSurName,
                    dayOfBirthValue = verificationData.personInfo.dayOfBirth,
                    monthOfBirthValue = verificationData.personInfo.monthOfBirth,
                    yearOfBirthValue = verificationData.personInfo.yearOfBirth,
                    buildingNumberValue = verificationData.location.buildingNumber,
                    streetNameValue = verificationData.location.streetName,
                    cityValue = verificationData.location.city,
                    provinceValue = verificationData.location.stateProvinceCode,
                    zipCodeValue = verificationData.location.postalCode,
                    ssnValue = verificationData.nationalIds.first().number,
                    sourceOfFunds = responseItem.sourceOfFunds ?: "",
                    occupation = responseItem.occupation ?: "",
                    firstNameValidationError = mapIdentityFieldsError.get(
                        VerificationIdentityFieldsType.FIRST_NAME
                    ) ?: false,
                    lastNameValidationError = mapIdentityFieldsError.get(
                        VerificationIdentityFieldsType.LAST_NAME
                    ) ?: false,
                    birthDateValidationError = mapIdentityFieldsError.get(
                        VerificationIdentityFieldsType.DAY_OF_BIRTH
                    ) ?: false || mapIdentityFieldsError.get(VerificationIdentityFieldsType.MONTH_OF_BIRTH) ?: false || mapIdentityFieldsError.get(
                        VerificationIdentityFieldsType.YEAR_OF_BIRTH
                    ) ?: false,
                    provinceValidationError = mapIdentityFieldsError.get(
                        VerificationIdentityFieldsType.PROVINCE
                    ) ?: false,
                    cityValidationError = mapIdentityFieldsError.get(VerificationIdentityFieldsType.CITY)
                        ?: false,
                    streetNameValidationError = mapIdentityFieldsError.get(
                        VerificationIdentityFieldsType.STREET_NAME
                    ) ?: false,
                    buildingNumberValidationError = mapIdentityFieldsError.get(
                        VerificationIdentityFieldsType.BUILDING_NUMBER
                    ) ?: false,
                    zipCodeValidationError = mapIdentityFieldsError.get(
                        VerificationIdentityFieldsType.ZIP_CODE
                    ) ?: false,
                    ssnValidationError = mapIdentityFieldsError.get(VerificationIdentityFieldsType.SSN)
                        ?: false,
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
            var hasErrors = false
            var overrideInvalidFields = false
            val mapDocumentFieldsError =
                mutableMapOf<VerificationDocumentFieldsType, Boolean>()
            if (RecordStatus.fromString(responseItem.documentVerification.response?.record?.recordStatus) == RecordStatus.NO_MATCH) {
                responseItem.documentVerification.response?.record?.datasourceResults?.let { listOfDataSource ->
                    for (dataSource in listOfDataSource) {
                        for (error in dataSource.errors) {
                            val fieldName = error.message?.substringAfterLast(":")?.trim()
                            mapDocumentFieldsError[VerificationDocumentFieldsType.fromString(
                                fieldName
                            )] = true
                            hasErrors = true
                        }
                    }
                }
            }
            if (RecordStatus.fromString(responseItem.documentVerification.response?.record?.recordStatus) == RecordStatus.NO_MATCH && !hasErrors) {
                overrideInvalidFields = true
            }

            Either.Right(
                VerificationDocumentResponseDataItem(
                    recordStatus = RecordStatus.fromString(responseItem.documentVerification.response?.record?.recordStatus),
                    documentType = DocumentType.fromString(responseItem.documentVerification.request?.documentType),
                    transactionId = responseItem.documentVerification.response?.transactionID,
                    frontImageBitmap = null,
                    backImageBitmap = null,
                    selfieImageBitmap = null,
                    frontImageValidationError = mapDocumentFieldsError.get(
                        VerificationDocumentFieldsType.FRONT_SCAN
                    ) ?: overrideInvalidFields,
                    backImageValidationError = mapDocumentFieldsError.get(
                        VerificationDocumentFieldsType.BACK_SCAN
                    ) ?: overrideInvalidFields,
                    selfieImageValidationError = mapDocumentFieldsError.get(
                        VerificationDocumentFieldsType.SELFIE
                    ) ?: overrideInvalidFields,
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
