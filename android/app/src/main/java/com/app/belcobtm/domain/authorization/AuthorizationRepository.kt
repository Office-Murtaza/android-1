package com.app.belcobtm.domain.authorization

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure

interface AuthorizationRepository {
    suspend fun clearAppData(): Either<Failure, Unit>
    suspend fun recoverWallet(phone: String, password: String): Either<Failure, Unit>
    suspend fun verifySmsCode(smsCode: String): Either<Failure, Unit>
}