package com.app.belcobtm.domain.authorization.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.authorization.AuthorizationRepository

class CheckPassUseCase(private val repository: AuthorizationRepository): UseCase<Boolean, CheckPassUseCase.Params>() {

    override suspend fun run(params: Params): Either<Failure, Boolean> = //repository.checkPass(params.userId, params.password)
        Either.Right(true)

    data class Params(val userId: String, val password: String)
}
