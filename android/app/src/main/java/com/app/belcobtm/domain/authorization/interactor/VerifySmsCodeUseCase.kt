package com.app.belcobtm.domain.authorization.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.authorization.AuthorizationRepository

class VerifySmsCodeUseCase(
    private val repository: AuthorizationRepository
) : UseCase<Unit, VerifySmsCodeUseCase.Params>() {

    override suspend fun run(params: Params): Either<Failure, Unit> = repository.verifySmsCode(params.smsCode)

    data class Params(val smsCode: String)
}