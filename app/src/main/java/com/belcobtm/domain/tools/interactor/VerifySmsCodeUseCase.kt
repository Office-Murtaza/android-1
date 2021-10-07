package com.belcobtm.domain.tools.interactor

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.tools.ToolsRepository

class VerifySmsCodeUseCase(
    private val repository: ToolsRepository
) : UseCase<Boolean, VerifySmsCodeUseCase.Params>() {

    override suspend fun run(params: Params): Either<Failure, Boolean> =
        repository.verifySmsCode(params.phone, params.code)

    data class Params(val phone: String, val code: String)
}