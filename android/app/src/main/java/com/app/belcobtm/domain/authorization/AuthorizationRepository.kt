package com.app.belcobtm.domain.authorization

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure

interface AuthorizationRepository {
    fun getAuthorizationStatus(): AuthorizationStatus
    fun getAuthorizePin(): String
    fun setAuthorizePin(pinCode: String)
    fun clearAppData()
    suspend fun authorizationCheckCredentials(
        phone: String,
        password: String
    ): Either<Failure, Pair<Boolean, Boolean>>

    suspend fun createSeedPhrase(): Either<Failure, String>

    suspend fun saveSeed(seed: String): Either<Failure, Unit>

    suspend fun createWallet(
        phone: String, password: String, notificationToken: String
    ): Either<Failure, Unit>

    suspend fun recoverWallet(
        seed: String, phone: String, password: String, notificationToken: String
    ): Either<Failure, Unit>

    suspend fun authorize(): Either<Failure, Unit>
    suspend fun checkPass(userId: String, password: String): Either<Failure, Boolean>
}