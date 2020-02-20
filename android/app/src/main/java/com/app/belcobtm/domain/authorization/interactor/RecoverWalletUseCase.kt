package com.app.belcobtm.domain.authorization.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.authorization.AuthorizationRepository

class RecoverWalletUseCase(
    private val repository: AuthorizationRepository
) : UseCase<Unit, RecoverWalletUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, Unit> = repository.recoverWallet(params.phone, params.password)

    data class Params(val phone: String, val password: String)
}