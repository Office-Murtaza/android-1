package com.belcobtm.data.rest.authorization

import android.os.Build
import com.belcobtm.BuildConfig
import com.belcobtm.data.rest.authorization.request.CheckCredentialsRequest
import com.belcobtm.data.rest.authorization.request.CheckPassRequest
import com.belcobtm.data.rest.authorization.request.CreateWalletCoinRequest
import com.belcobtm.data.rest.authorization.request.CreateWalletRequest
import com.belcobtm.data.rest.authorization.request.RecoverWalletCoinRequest
import com.belcobtm.data.rest.authorization.request.RecoverWalletRequest
import com.belcobtm.data.rest.authorization.request.RefreshTokenRequest
import com.belcobtm.data.rest.authorization.response.AuthorizationResponse
import com.belcobtm.data.rest.authorization.response.CheckPassResponse
import com.belcobtm.data.rest.authorization.response.CreateRecoverWalletResponse
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import java.net.HttpURLConnection

class AuthApiService(private val authApi: AuthApi) {

    companion object {

        private val deviceOS = Build.VERSION.RELEASE
        private val deviceModel = Build.MODEL
        private const val appVersion = BuildConfig.VERSION_NAME
    }

    suspend fun authorizationCheckCredentials(
        phone: String,
        password: String,
        email: String
    ): Either<Failure, Triple<Boolean, Boolean, Boolean>> = try {
        val request = authApi.authorizationCheckCredentialsAsync(CheckCredentialsRequest(phone, password, email))
        request.body()?.let { Either.Right(Triple(it.phoneExists, it.passwordsMatch, it.emailExists)) }
            ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        Either.Left(failure)
    }

    suspend fun createWallet(
        phone: String,
        password: String,
        email: String,
        lat: Double?,
        lng: Double?,
        timezone: String,
        notificationToken: String?,
        coinMap: Map<String, String>
    ): Either<Failure, CreateRecoverWalletResponse> = try {
        val coinList = coinMap.map { CreateWalletCoinRequest(it.key, it.value) }
        val request = CreateWalletRequest(
            phone,
            password,
            email,
            deviceModel,
            deviceOS,
            appVersion,
            lat,
            lng,
//           40.74371337890625,
//            -73.980727954218736,
            timezone,
            notificationToken,
            coinList,
            "ANDROID"
        )
        val response = authApi.createWalletAsync(request)
        response.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun recoverWallet(
        phone: String,
        password: String,
        lat: Double?,
        lng: Double?,
        timezone: String,
        notificationToken: String?,
        coinMap: Map<String, String>
    ): Either<Failure, CreateRecoverWalletResponse> = try {
        val coinList = coinMap.map { RecoverWalletCoinRequest(it.key, it.value) }
        val request = RecoverWalletRequest(
            phone,
            password,
            deviceModel,
            deviceOS,
            appVersion,
            lat,
            lng,
//            40.74371337890625,
//            -73.980727954218736,
            timezone,
            notificationToken,
            coinList
        )
        val response = authApi.recoverWalletAsync(request)
        response.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        if (failure.message == "No value for errorMsg") {
            Either.Left(Failure.MessageError(failure.message))
        } else {
            Either.Left(failure)
        }
    }

    suspend fun authorizeByRefreshToken(refreshToken: String): Either<Failure, AuthorizationResponse> = try {
        val request = authApi.signInByRefreshTokenAsync(RefreshTokenRequest(refreshToken))
        request.body()?.let { Either.Right(it) } ?: when (request.code()) {
            HttpURLConnection.HTTP_UNAUTHORIZED,
            HttpURLConnection.HTTP_FORBIDDEN -> Either.Left(Failure.TokenError)
            else -> Either.Left(Failure.ServerError())
        }
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun checkPass(userId: String, password: String): Either<Failure, CheckPassResponse> =
        try {
            val request = authApi.checkPass(userId, CheckPassRequest(password))
            request.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
        } catch (failure: Failure) {
            failure.printStackTrace()
            Either.Left(failure)
        }

}
