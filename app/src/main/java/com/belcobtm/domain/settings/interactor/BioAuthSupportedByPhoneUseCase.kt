package com.belcobtm.domain.settings.interactor

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.settings.SettingsRepository

class BioAuthSupportedByPhoneUseCase(
    private val settingsRepository: SettingsRepository
    ) : UseCase<Boolean, UseCase.None>() {

    override suspend fun run(params: None): Either<Failure, Boolean> {
        return settingsRepository.bioAuthSupportedByPhone()
    }
}
