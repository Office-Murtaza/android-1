package com.belcobtm.domain.tools.interactor

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.tools.ToolsRepository

class OldSendSmsToDeviceUseCase(private val repository: ToolsRepository) : UseCase<Unit, Unit>() {
    override suspend fun run(params: Unit): Either<Failure, Unit> = repository.sendSmsToDeviceOld()
}