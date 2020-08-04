package com.app.belcobtm.domain.settings.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.settings.SettingsRepository

class GetPhoneUseCase(private val settingsRepository: SettingsRepository): UseCase<String, Unit>() {
    override suspend fun run(params: Unit): Either<Failure, String> {
        return settingsRepository.getPhone()
    }
}