package com.app.belcobtm.data.rest.settings

import com.app.belcobtm.data.rest.settings.response.VerificationInfoResponse
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure

class SettingsApiService(private val api: SettingsApi) {
    suspend fun getVerificationInfo(userId: Int): Either<Failure, VerificationInfoResponse> = try {
        val request = api.getVerificationInfoAsync(userId).await()
        request.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }
}