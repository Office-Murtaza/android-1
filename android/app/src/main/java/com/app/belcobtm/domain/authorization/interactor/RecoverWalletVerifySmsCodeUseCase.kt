package com.app.belcobtm.domain.authorization.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.authorization.AuthorizationRepository

class RecoverWalletVerifySmsCodeUseCase(
    private val repository: AuthorizationRepository
) : UseCase<Unit, RecoverWalletVerifySmsCodeUseCase.Params>() {

    override suspend fun run(params: Params): Either<Failure, Unit> = repository.recoverWalletVerifySmsCode(params.smsCode)

    data class Params(val smsCode: String)
}