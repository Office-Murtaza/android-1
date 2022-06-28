package com.belcobtm.domain.settings.interactor

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.settings.SettingsRepository

class UpdatePhoneUseCase(
    private val settingsRepository: SettingsRepository
) : UseCase<Boolean, Unit>() {

    override suspend fun run(params: Unit): Either<Failure, Boolean> {
        return settingsRepository.updatePhone()
    }

}
