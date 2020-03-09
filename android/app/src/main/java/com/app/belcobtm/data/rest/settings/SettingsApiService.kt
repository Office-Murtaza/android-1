package com.app.belcobtm.data.rest.settings

import com.app.belcobtm.data.core.FileHelper
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
            createTextPart(blankItem.tierId.toString()),
            createTextPart(blankItem.idNumber),
            createTextPart(blankItem.firstName),
            createTextPart(blankItem.lastName),
            createTextPart(blankItem.address),
            createTextPart(blankItem.city),
            createTextPart(blankItem.country),
            createTextPart(blankItem.province),
            createTextPart(blankItem.zipCode),
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
            createTextPart(dataItem.tierId.toString()),
            createTextPart(dataItem.ssn),
            fileHelper.createFilePart(compressedFile.path, "image/*", "file")
        ).await()

        request.body()?.let { Either.Right(Unit) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    private fun createTextPart(text: String) = RequestBody.create(MediaType.parse("text/plain"), text)
}