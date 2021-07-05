package com.belcobtm.domain.tools.interactor

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.tools.ToolsRepository

class SendSmsToDeviceUseCase(
    private val repository: ToolsRepository
) :
    UseCase<String, SendSmsToDeviceUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, String> = repository.sendSmsToDevice(params.phone)

    data class Params(val phone: String)
}