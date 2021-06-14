package com.app.belcobtm.domain.settings.interactor

import com.app.belcobtm.data.core.UnlinkHandler
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.settings.SettingsRepository

class UnlinkUseCase(private val unlinkHandler: UnlinkHandler) : UseCase<Unit, Unit>() {

    override suspend fun run(params: Unit): Either<Failure, Unit> =
        Either.Right(unlinkHandler.performUnlink(openInitialScreen = false))
}