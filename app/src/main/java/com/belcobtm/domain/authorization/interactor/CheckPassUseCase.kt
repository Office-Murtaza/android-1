package com.belcobtm.domain.authorization.interactor

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.authorization.AuthorizationRepository

class CheckPassUseCase(private val repository: AuthorizationRepository): UseCase<Boolean, CheckPassUseCase.Params>() {

    override suspend fun run(params: Params): Either<Failure, Boolean> = repository.checkPass(params.userId, params.password)

    data class Params(val userId: String, val password: String)
}
