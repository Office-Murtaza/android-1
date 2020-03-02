package com.app.belcobtm.domain.authorization.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.authorization.AuthorizationRepository

class CreateWalletVerifySmsCodeUseCase(
    private val repository: AuthorizationRepository
) : UseCase<String, CreateWalletVerifySmsCodeUseCase.Params>() {

    override suspend fun run(params: Params): Either<Failure, String> = repository.createWalletVerifySmsCode(params.smsCode)

    data class Params(val smsCode: String)
}