package com.belcobtm.domain.tools.interactor

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.tools.ToolsRepository

class OldVerifySmsCodeUseCase(private val repository: ToolsRepository) :
    UseCase<Unit, OldVerifySmsCodeUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, Unit> = repository.verifySmsCodeOld(params.smsCode)

    data class Params(val smsCode: String)
}