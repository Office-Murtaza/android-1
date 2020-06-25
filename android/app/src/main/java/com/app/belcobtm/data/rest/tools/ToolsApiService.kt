package com.app.belcobtm.data.rest.tools

import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.data.rest.transaction.request.VerifySmsCodeRequest
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure

class ToolsApiService(
    private val api: ToolsApi,
    private val prefHelper: SharedPreferencesHelper
) {
    suspend fun sendToDeviceSmsCode(): Either<Failure, Unit> = try {
        val request = api.sendSmsCodeAsync(prefHelper.userId).await()
        request.body()?.let {
            if (request.isSuccessful) {
                Either.Right(Unit)
            } else {
                Either.Left(Failure.ServerError())
            }
        } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun verifySmsCode(smsCode: String): Either<Failure, Unit> = try {
        val request = api.verifySmsCodeAsync(prefHelper.userId, VerifySmsCodeRequest(smsCode)).await()
        request.body()?.let { Either.Right(Unit) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }
}