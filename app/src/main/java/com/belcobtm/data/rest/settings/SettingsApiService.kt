package com.belcobtm.data.rest.settings

import com.belcobtm.data.rest.settings.request.*
import com.belcobtm.data.rest.settings.response.*
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.settings.item.*

class SettingsApiService(private val api: SettingsApi) {

    suspend fun getVerificationInfo(userId: String): Either<Failure, VerificationInfoResponse> =
        try {
            val request = api.getVerificationInfoAsync(userId).await()
            request.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
        } catch (failure: Failure) {
            failure.printStackTrace()
            Either.Left(failure)
        }

    suspend fun getVerificationDetails(userId: String): Either<Failure, VerificationDetailsResponse> =
        try {
            val request = api.getVerificationDetailsAsync(userId).await()
            request.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
        } catch (failure: Failure) {
            failure.printStackTrace()
            Either.Left(failure)
        }

    suspend fun getVerificationFields(countryCode: String): Either<Failure, VerificationFieldsResponse> =
        try {
            val request = api.getVerificationFieldsAsync(countryCode).await()
            request.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
        } catch (failure: Failure) {
            failure.printStackTrace()
            Either.Left(failure)
        }

    suspend fun sendVerificationIdentity(
        userId: String,
        telephone: String,
        identityDataItem: VerificationIdentityDataItem
    ): Either<Failure, VerificationUserIdentityResponse> = try {
        val request = with(identityDataItem) {
            VerificationUserIdentityRequest(
                countryCode = countryCode,
                occupation = occupation,
                sourceOfFunds = sourceOfFunds,
                verificationData = VerificationData(
                    personInfo = PersonInfo(
                        firstGivenName = firstName,
                        firstSurName = lastName,
                        dayOfBirth = dayOfBirth,
                        monthOfBirth = monthOfBirth,
                        yearOfBirth = yearOfBirth
                    ),
                    location = LocationInfo(
                        buildingNumber = buildingNumber,
                        streetName = streetName,
                        city = city,
                        stateProvinceCode = province,
                        postalCode = zipCode
                    ),
                    communication = CommunicationInfo(
                        telephone = telephone
                    ),
                    nationalIds = listOf<NationalId>(
                        NationalId(
                            number = ssn,
                            type = "socialservice"
                        )
                    )

                )
            )
        }
        val response = api.sendVerificationIdentityAsync(userId, request).await()
        response.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun sendVerificationDocument(
        userId: String,
        documentDataItem: VerificationDocumentDataItem,
        firebaseImages: VerificationDocumentFirebaseImages
    ): Either<Failure, VerificationUserDocumentResponse> = try {
        val request =
            VerificationDocumentRequest(
                countryCode = documentDataItem.countryDataItem.code,
                documentType = documentDataItem.documentType.stringValue,
                base64 = Base64VerificationDocuments(
                    frontImage = documentDataItem.frontScanBase64,
                    backImage = documentDataItem.backScanBase64,
                    selfieImage = documentDataItem.selfieBase64
                ),
                firebase = FirebaseVerificationDocuments(
                    frontImage = firebaseImages.frontDocumentFileName,
                    backImage = firebaseImages.backDocumentFileName,
                    selfieImage = firebaseImages.selfieFileName,
                )
            )
        val response = api.sendVerificationDocumentAsync(userId, request).await()
        response.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }


    suspend fun sendVerificationBlank(
        userId: String,
        blankItem: VerificationBlankDataItem,
        fileName: String
    ): Either<Failure, Unit> = try {
        val request = with(blankItem) {
            VerificationBlankRequest(
                id,
                fileName,
                idNumber,
                firstName,
                lastName,
                address,
                city,
                country,
                province,
                zipCode
            )
        }
        val response = api.sendVerificationBlankAsync(userId, request).await()
        response.body()?.let { Either.Right(Unit) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun sendVerificationVip(
        userId: String,
        dataItem: VerificationVipDataItem,
        fileName: String
    ): Either<Failure, Unit> = try {
        val request = VipVerificationRequest(
            dataItem.id,
            dataItem.idCardNumberFilename,
            dataItem.idCardNumber,
            dataItem.firstName,
            dataItem.lastName,
            dataItem.address,
            dataItem.city,
            dataItem.country,
            dataItem.province,
            dataItem.zipCode,
            dataItem.ssn.toString(),
            fileName
        )
        val response = api.sendVerificationVipAsync(userId, request).await()
        response.body()?.let { Either.Right(Unit) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun changePass(
        userId: String,
        oldPassword: String,
        newPassword: String
    ): Either<Failure, Boolean> = try {
        val request = api.changePass(userId, ChangePassBody(newPassword, oldPassword)).await()
        request.body()?.let { Either.Right(it.result) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun getPhone(
        userId: String
    ): Either<Failure, String> = try {
        val request = api.getPhone(userId).await()

        request.body()?.let { Either.Right(it.phone) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun updatePhone(
        userId: String,
        newPhone: String
    ): Either<Failure, Boolean> = try {
        val request = api.updatePhone(
            userId,
            UpdatePhoneParam(newPhone)
        ).await()

        request.body()?.let { Either.Right(it.result) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun verifyPhone(
        userId: String,
        newPhone: String
    ): Either<Failure, Boolean> = try {
        val request = api.verifyPhone(
            userId,
            UpdatePhoneParam(newPhone)
        ).await()

        request.body()?.let { Either.Right(it.result) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }
}