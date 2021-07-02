package com.app.belcobtm.domain.tools.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.tools.ToolsRepository

class OldSendSmsToDeviceUseCase(private val repository: ToolsRepository) : UseCase<Unit, Unit>() {
    override suspend fun run(params: Unit): Either<Failure, Unit> = repository.sendSmsToDeviceOld()
}