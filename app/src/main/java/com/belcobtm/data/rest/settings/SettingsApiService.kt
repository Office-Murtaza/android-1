package com.belcobtm.data.rest.settings

import com.belcobtm.data.rest.settings.request.ChangePassBody
import com.belcobtm.data.rest.settings.request.UpdatePhoneParam
import com.belcobtm.data.rest.settings.request.VerificationBlankRequest
import com.belcobtm.data.rest.settings.request.VipVerificationRequest
import com.belcobtm.data.rest.settings.response.VerificationInfoResponse
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.settings.item.VerificationBlankDataItem
import com.belcobtm.domain.settings.item.VerificationVipDataItem
import com.belcobtm.domain.settings.type.VerificationStatus

class SettingsApiService(private val api: SettingsApi) {

    suspend fun getVerificationInfo(userId: String): Either<Failure, VerificationInfoResponse> =
        try {
            val request = api.getVerificationInfoAsync(userId).await()
            request.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
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