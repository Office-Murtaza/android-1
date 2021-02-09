package com.app.belcobtm.domain.settings.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.settings.SettingsRepository

class SetBioAuthStateAllowedUseCase(
    private val settingsRepository: SettingsRepository
) : UseCase<Unit, SetBioAuthStateAllowedUseCase.Params>() {

    override suspend fun run(params: Params): Either<Failure, Unit> {
        return Either.Right(settingsRepository.setUserAllowedBioAuth(params.allowed))
    }

    data class Params(val allowed: Boolean)
}
