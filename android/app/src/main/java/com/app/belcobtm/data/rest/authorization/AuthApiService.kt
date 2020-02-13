package com.app.belcobtm.data.rest.authorization

import com.app.belcobtm.data.rest.authorization.request.RecoverWalletRequest
import com.app.belcobtm.data.rest.authorization.request.VerifySmsCodeRequest
import com.app.belcobtm.data.rest.authorization.response.RecoverWalletResponse
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure

class AuthApiService(private val authApi: AuthApi) {
    suspend fun recoverWallet(phone: String, password: String): Either<Failure, RecoverWalletResponse> = try {
        val request = authApi.recoverWalletAsync(RecoverWalletRequest(phone, password)).await()
        request.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun verifySmsCode(userId: Int, smsCode: String): Either<Failure, Unit> = try {
        authApi.verifySmsCodeAsync(userId, VerifySmsCodeRequest(smsCode)).await()
        Either.Right(Unit)
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }
}

