package com.app.belcobtm.domain.settings.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.settings.SettingsRepository

class BioAuthAllowedByUserUseCase(
    private val settingsRepository: SettingsRepository
) : UseCase<Boolean, UseCase.None>() {

    override suspend fun run(params: None): Either<Failure, Boolean> {
        return settingsRepository.userAllowedBioAuth()
    }
}
