package com.app.belcobtm.domain.tools.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.tools.ToolsRepository

class SendSmsToDeviceUseCase(
    private val repository: ToolsRepository
) :
    UseCase<String, SendSmsToDeviceUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, String> = repository.sendSmsToDevice(params.phone)

    data class Params(val phone: String)
}