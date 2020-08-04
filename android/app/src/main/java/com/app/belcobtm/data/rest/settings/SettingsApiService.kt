package com.app.belcobtm.data.rest.settings

import com.app.belcobtm.data.core.FileHelper
import com.app.belcobtm.data.rest.settings.request.ChangePassBody
import com.app.belcobtm.data.rest.settings.response.UpdatePhoneParam
import com.app.belcobtm.data.rest.settings.response.VerificationInfoResponse
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.settings.item.VerificationBlankDataItem
import com.app.belcobtm.domain.settings.item.VerificationVipDataItem
import okhttp3.MediaType
import okhttp3.RequestBody

class SettingsApiService(private val fileHelper: FileHelper, private val api: SettingsApi) {
    suspend fun getVerificationInfo(userId: Int): Either<Failure, VerificationInfoResponse> = try {
        val request = api.getVerificationInfoAsync(userId).await()
        request.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun sendVerificationBlank(
        userId: Int,
        blankItem: VerificationBlankDataItem
    ): Either<Failure, Unit> = try {
        val compressedFile = fileHelper.compressImageFile(blankItem.imageUri)
        val request = api.sendVerificationBlankAsync(
            userId,
            VERIFICATION,
            RequestBody.create(MediaType.parse("text/plain"), blankItem.idNumber),
            RequestBody.create(MediaType.parse("text/plain"), blankItem.firstName),
            RequestBody.create(MediaType.parse("text/plain"), blankItem.lastName),
            RequestBody.create(MediaType.parse("text/plain"), blankItem.address),
            RequestBody.create(MediaType.parse("text/plain"), blankItem.city),
            RequestBody.create(MediaType.parse("text/plain"), blankItem.country),
            RequestBody.create(MediaType.parse("text/plain"), blankItem.province),
            RequestBody.create(MediaType.parse("text/plain"), blankItem.zipCode),
            fileHelper.createFilePart(compressedFile.path, "image/*", "file")
        ).await()

        request.body()?.let { Either.Right(Unit) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun sendVerificationVip(
        userId: Int,
        dataItem: VerificationVipDataItem
    ): Either<Failure, Unit> = try {
        val compressedFile = fileHelper.compressImageFile(dataItem.fileUri)
        val request = api.sendVerificationVipAsync(
            userId,
            VIP_VERIFICATION,
            dataItem.ssn,
            fileHelper.createFilePart(compressedFile.path, "image/*", "file")
        ).await()

        request.body()?.let { Either.Right(Unit) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun unlink(
        userId: Int
    ): Either<Failure, Boolean> = try {
        val request = api.unlink(userId.toString()).await()

        request.body()?.let { Either.Right(it.result) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun changePass(userId: Int, oldPassword: String, newPassword: String) : Either<Failure, Boolean> = try {
        val request = api.changePass(userId.toString(), ChangePassBody(newPassword, oldPassword)).await()

        request.body()?.let { Either.Right(it.result) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun getPhone(
        userId: Int
    ): Either<Failure, String> = try {
        val request = api.getPhone(userId.toString()).await()

        request.body()?.let { Either.Right(it.phone) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun updatePhone(
        userId: Int,
        newPhone: String
    ): Either<Failure, Boolean> = try {
        val request = api.updatePhone(userId.toString(), UpdatePhoneParam(newPhone)).await()

        request.body()?.let { Either.Right(it.result) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }
    companion object {
        private const val VERIFICATION = 1
        private const val VIP_VERIFICATION = 2
    }
}