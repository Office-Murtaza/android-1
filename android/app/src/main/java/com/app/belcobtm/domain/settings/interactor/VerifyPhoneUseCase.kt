package com.app.belcobtm.domain.settings.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.settings.SettingsRepository

class VerifyPhoneUseCase(private val settingsRepository: SettingsRepository): UseCase<Boolean, VerifyPhoneUseCase.Params>() {

    override suspend fun run(params: Params): Either<Failure, Boolean> {
        return settingsRepository.verifyPhone(params.phone)
    }

    data class Params(val phone: String)
}