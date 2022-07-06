package com.belcobtm.domain.authorization

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.settings.type.VerificationStatus

interface AuthorizationRepository {

    fun getAuthorizationStatus(): AuthorizationStatus
    fun getAuthorizePin(): String
    fun setAuthorizePin(pinCode: String)
    fun setIsUserAuthed(isAuthed: Boolean)
    fun clearAppData()
    fun getVerificationStatus(): Either<Failure, VerificationStatus>
    suspend fun authorizationCheckCredentials(
        phone: String,
        password: String,
        email: String
    ): Either<Failure, Triple<Boolean, Boolean, Boolean>>

    suspend fun createSeedPhrase(): Either<Failure, String>

    suspend fun saveSeed(seed: String): Either<Failure, Unit>

    suspend fun createWallet(
        phone: String,
        password: String,
        email: String,
        notificationToken: String
    ): Either<Failure, Unit>

    suspend fun recoverWallet(
        seed: String,
        phone: String,
        password: String,
        notificationToken: String
    ): Either<Failure, Unit>

    suspend fun authorize(): Either<Failure, Unit>

}
