package com.belcobtm.domain.settings.interactor

import com.belcobtm.data.core.UnlinkHandler
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase

class UnlinkUseCase(private val unlinkHandler: UnlinkHandler) : UseCase<Unit, Unit>() {

    override suspend fun run(params: Unit): Either<Failure, Unit> =
        Either.Right(unlinkHandler.performUnlink(openInitialScreen = false))
}