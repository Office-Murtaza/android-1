package com.belcobtm.data.rest.tools

import com.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.belcobtm.data.rest.authorization.request.VerifyPhoneRequest
import com.belcobtm.data.rest.transaction.request.VerifySmsCodeRequest
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure

class ToolsApiService(
    private val api: ToolsApi,
    private val prefHelper: SharedPreferencesHelper
) {

    suspend fun verifyPhone(phone: String): Either<Failure, String> = try {
        val request = api.verifyPhoneAsync(VerifyPhoneRequest(phone)).await()
        request.body()?.let { Either.Right(it.code) }
            ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        Either.Left(failure)
    }

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