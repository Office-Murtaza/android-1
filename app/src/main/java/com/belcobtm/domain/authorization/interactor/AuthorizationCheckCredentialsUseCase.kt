package com.belcobtm.domain.authorization.interactor

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.authorization.AuthorizationRepository

const val AUTH_ERROR_PHONE_NOT_SUPPORTED = 2
//hack due to backend limitation.
class AuthorizationCheckCredentialsUseCase(
    private val repository: AuthorizationRepository
) : UseCase<Pair<Boolean, Boolean>, AuthorizationCheckCredentialsUseCase.Params>() {

    override suspend fun run(params: Params): Either<Failure, Pair<Boolean, Boolean>>  =
        repository.authorizationCheckCredentials(params.phone, params.password)

    data class Params(val phone: String, val password: String)
}