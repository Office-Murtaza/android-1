package com.app.belcobtm.domain.settings.interactor

import com.app.belcobtm.data.core.UnlinkHandler
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.settings.SettingsRepository

class UnlinkUseCase(
    private val repository: SettingsRepository,
    private val unlinkHandler: UnlinkHandler
) : UseCase<Boolean, Unit>() {

    override suspend fun run(params: Unit): Either<Failure, Boolean> =
        unlinkHandler.performUnlink(openInitialScreen = false)
}