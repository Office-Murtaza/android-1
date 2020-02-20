package com.app.belcobtm.data

import com.app.belcobtm.data.core.NetworkUtils
import com.app.belcobtm.data.rest.authorization.AuthApiService
import com.app.belcobtm.data.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.db.DbCryptoCoinModel
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.authorization.AuthorizationRepository
import io.realm.Realm

class AuthorizationRepositoryImpl(
    private val prefHelper: SharedPreferencesHelper,
    private val apiService: AuthApiService,
    private val networkUtils: NetworkUtils
) : AuthorizationRepository {

    override suspend fun clearAppData(): Either<Failure, Unit> {
        prefHelper.accessToken = null
        prefHelper.refreshToken = null
        prefHelper.userPin = null
        prefHelper.userId = null

        val realm = Realm.getDefaultInstance()
        DbCryptoCoinModel().delAllCryptoCoin(realm)

        return Either.Right(Unit)
    }

    override suspend fun recoverWallet(
        phone: String,
        password: String
    ): Either<Failure, Unit> = if (networkUtils.isNetworkAvailable()) {
        val response = apiService.recoverWallet(phone, password)

        if (response.isRight) {
            val body = (response as Either.Right).b
            prefHelper.accessToken = body.accessToken
            prefHelper.refreshToken = body.refreshToken
            prefHelper.userId = body.userId

            Either.Right(Unit)
        } else {
            response as Either.Left
        }
    } else {
        Either.Left(Failure.NetworkConnection)
    }

    override suspend fun verifySmsCode(
        smsCode: String
    ): Either<Failure, Unit> = if (networkUtils.isNetworkAvailable()) {
        apiService.verifySmsCode(prefHelper.userId ?: -1, smsCode)
    } else {
        Either.Left(Failure.NetworkConnection)
    }
}