package com.app.belcobtm.domain.authorization.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.authorization.AuthorizationRepository

class CreateWalletUseCase(private val repository: AuthorizationRepository) :
    UseCase<Unit, CreateWalletUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, Unit> =
        repository.createWallet(params.phone, params.password)

    data class Params(val phone: String, val password: String)
}