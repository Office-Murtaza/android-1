package com.app.belcobtm.domain.tools.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.tools.ToolsRepository

class VerifySmsCodeUseCase(private val repository: ToolsRepository) : UseCase<Unit, VerifySmsCodeUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, Unit> = repository.verifySmsCode(params.smsCode)

    data class Params(val smsCode: String)
}