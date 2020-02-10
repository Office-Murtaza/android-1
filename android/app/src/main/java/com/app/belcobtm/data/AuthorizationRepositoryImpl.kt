package com.app.belcobtm.data

import com.app.belcobtm.data.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.db.DbCryptoCoinModel
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.authorization.AuthorizationRepository
import io.realm.Realm

class AuthorizationRepositoryImpl(private val prefsHelper: SharedPreferencesHelper) : AuthorizationRepository {

    override suspend fun clearAppData(): Either<Failure, Unit> {
        prefsHelper.accessToken = ""
        prefsHelper.refreshToken = ""
        prefsHelper.userPin = ""
        prefsHelper.userId = -1

        val realm = Realm.getDefaultInstance()
        DbCryptoCoinModel().delAllCryptoCoin(realm)

        return Either.Right(Unit)
    }
}