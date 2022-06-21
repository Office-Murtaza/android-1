package com.belcobtm.domain.authorization.interactor

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.authorization.AuthorizationRepository

const val AUTH_ERROR_PHONE_NOT_SUPPORTED = 2

//hack due to backend limitation.
class AuthorizationCheckCredentialsUseCase(
    private val repository: AuthorizationRepository
) : UseCase<Triple<Boolean, Boolean, Boolean>, AuthorizationCheckCredentialsUseCase.Params>() {

    override suspend fun run(params: Params): Either<Failure, Triple<Boolean, Boolean, Boolean>> =
        repository.authorizationCheckCredentials(params.phone, params.password, params.email)

    data class Params(val phone: String, val password: String, val email: String = "")

}
