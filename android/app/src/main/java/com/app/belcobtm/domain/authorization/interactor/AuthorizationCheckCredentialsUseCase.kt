package com.app.belcobtm.domain.authorization.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.authorization.AuthorizationRepository
import com.app.belcobtm.domain.tools.ToolsRepository

const val AUTH_ERROR_PHONE_NOT_SUPPORTED = 2
//hack due to backend limitation.
class AuthorizationCheckCredentialsUseCase(
    private val repository: AuthorizationRepository,
    private val toolsRepository: ToolsRepository
) : UseCase<Pair<Boolean, Boolean>, AuthorizationCheckCredentialsUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, Pair<Boolean, Boolean>> {
        val authResult = repository.authorizationCheckCredentials(params.phone, params.password)
        if (authResult.isRight) {
            val toolsResult = toolsRepository.sendSmsToDevice(params.phone)
            if (toolsResult.isRight) {
                return authResult
            } else {
                throw (toolsResult as Either.Left).a
            }
        } else {
            return authResult
        }
    }

    data class Params(val phone: String, val password: String)
}