package com.belcobtm.domain.settings.interactor

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.settings.SettingsRepository

class SetBioAuthStateAllowedUseCase(
    private val settingsRepository: SettingsRepository
) : UseCase<Unit, SetBioAuthStateAllowedUseCase.Params>() {

    override suspend fun run(params: Params): Either<Failure, Unit> {
        return Either.Right(settingsRepository.setUserAllowedBioAuth(params.allowed))
    }

    data class Params(val allowed: Boolean)
}
