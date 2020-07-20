package com.app.belcobtm.data.rest.authorization

import com.app.belcobtm.data.rest.authorization.request.*
import com.app.belcobtm.data.rest.authorization.response.AuthorizationResponse
import com.app.belcobtm.data.rest.authorization.response.CreateRecoverWalletResponse
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure

class AuthApiService(private val authApi: AuthApi) {

    suspend fun authorizationCheckCredentials(
        phone: String,
        password: String
    ): Either<Failure, Pair<Boolean, Boolean>> = try {
        val request = authApi.authorizationCheckCredentialsAsync(CheckCredentialsRequest(phone, password)).await()
        request.body()?.let { Either.Right(Pair(it.phoneExist, it.passwordMatch)) }
            ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        Either.Left(failure)
    }

    suspend fun createWallet(
        phone: String,
        password: String,
        coinMap: Map<String, String>
    ): Either<Failure, CreateRecoverWalletResponse> = try {
        val coinList = coinMap.map { CreateWalletCoinRequest(it.key, it.value) }
        val request = authApi.createWalletAsync(CreateWalletRequest(phone, password, coinList)).await()
        request.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun recoverWallet(
        phone: String,
        password: String,
        coinMap: Map<String, String>
    ): Either<Failure, CreateRecoverWalletResponse> = try {
        val coinList = coinMap.map { RecoverWalletCoinRequest(it.key, it.value) }
        val request = authApi.recoverWalletAsync(RecoverWalletRequest(phone, password, coinList)).await()
        request.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun authorizeByRefreshToken(refreshToken: String): Either<Failure, AuthorizationResponse> = try {
        val request = authApi.signInByRefreshTokenAsync(RefreshTokenRequest(refreshToken)).await()
        request.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }
}

