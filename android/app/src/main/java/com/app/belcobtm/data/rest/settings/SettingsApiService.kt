package com.app.belcobtm.data.rest.settings

import com.app.belcobtm.data.core.FileHelper
import com.app.belcobtm.data.rest.settings.response.VerificationInfoResponse
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.settings.item.VerificationBlankDataItem
import com.app.belcobtm.domain.settings.item.VerificationVipDataItem

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
            blankItem.idNumber,
            blankItem.firstName,
            blankItem.lastName,
            blankItem.address,
            blankItem.city,
            blankItem.country,
            blankItem.province,
            blankItem.zipCode,
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

    companion object {
        private const val VERIFICATION = 1
        private const val VIP_VERIFICATION = 2
    }
}