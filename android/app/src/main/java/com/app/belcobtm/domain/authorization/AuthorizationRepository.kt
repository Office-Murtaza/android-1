package com.app.belcobtm.domain.authorization

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure

interface AuthorizationRepository {
    suspend fun clearAppData(): Either<Failure, Unit>
    suspend fun recoverWallet(phone: String, password: String): Either<Failure, Unit>
    suspend fun recoverWalletVerifySmsCode(smsCode: String): Either<Failure, Unit>
    suspend fun createWallet(phone: String, password: String): Either<Failure, Unit>
    suspend fun createWalletVerifySmsCode(smsCode: String): Either<Failure, String>
    suspend fun authorize(): Either<Failure, Unit>
    fun getAuthorizePin(): String
    fun setAuthorizePin(pinCode: String)
}