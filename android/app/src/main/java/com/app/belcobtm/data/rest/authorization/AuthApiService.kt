package com.app.belcobtm.data.rest.authorization

import com.app.belcobtm.data.rest.authorization.request.*
import com.app.belcobtm.data.rest.authorization.response.AddCoinsResponse
import com.app.belcobtm.data.rest.authorization.response.AuthorizationResponse
import com.app.belcobtm.data.rest.authorization.response.RecoverWalletResponse
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.wallet.item.AccountDataItem

class AuthApiService(private val authApi: AuthApi) {

    suspend fun checkCredentials(phone: String, password: String): Either<Failure, Pair<Boolean, Boolean>> = try {
        val request = authApi.checkCredentialsAsync(CheckCredentialsRequest(phone, password)).await()
        request.body()?.let { Either.Right(Pair(it.phoneExist, it.passwordMatch)) }
            ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        Either.Left(failure)
    }

    suspend fun verifyPhone(phone: String): Either<Failure, String> = try {
        val request = authApi.verifyPhoneAsync(VerifyPhoneRequest(phone)).await()
        request.body()?.let { Either.Right(it.code) }
            ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        Either.Left(failure)
    }


    suspend fun recoverWallet(phone: String, password: String): Either<Failure, RecoverWalletResponse> = try {
        val request = authApi.recoverWalletAsync(RecoverWalletRequest(phone, password)).await()
        request.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun recoverWalletVerifySmsCode(userId: Int, smsCode: String): Either<Failure, Unit> = try {
        authApi.recoverWalletVerifySmsCodeAsync(userId, VerifySmsCodeRequest(smsCode)).await()
        Either.Right(Unit)
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun registerWallet(phone: String, password: String): Either<Failure, AuthorizationResponse> = try {
        val request = authApi.createWalletAsync(CreateWalletRequest(phone, password)).await()
        request.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun createWalletVerifySmsCode(userId: Int, smsCode: String): Either<Failure, Unit> = try {
        authApi.createWalletVerifySmsCodeAsync(userId, CreateWalletVerifySmsCodeRequest(smsCode)).await()
        Either.Right(Unit)
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun addCoins(userId: Int, accountList: List<AccountDataItem>): Either<Failure, AddCoinsResponse> = try {
        val request = authApi.addCoinsAsync(
            userId,
            AddCoinsRequest(accountList.map { CoinRequest(it.type.name, it.publicKey) })
        ).await()
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

