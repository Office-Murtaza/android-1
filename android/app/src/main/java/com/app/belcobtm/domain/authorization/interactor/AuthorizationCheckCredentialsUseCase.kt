package com.app.belcobtm.domain.authorization.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.authorization.AuthorizationRepository

class AuthorizationCheckCredentialsUseCase(
    private val repository: AuthorizationRepository
) : UseCase<Boolean, AuthorizationCheckCredentialsUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, Boolean> =
        repository.authorizationCheckCredentials(params.phone, params.password)

    data class Params(val phone: String, val password: String)
}