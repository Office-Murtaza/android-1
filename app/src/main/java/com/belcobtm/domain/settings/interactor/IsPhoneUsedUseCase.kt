package com.belcobtm.domain.settings.interactor

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.settings.SettingsRepository

class IsPhoneUsedUseCase(private val settingsRepository: SettingsRepository) : UseCase<Boolean, IsPhoneUsedUseCase.Params>() {

    override suspend fun run(params: Params): Either<Failure, Boolean> {
        return settingsRepository.isPhoneUsed(params.phone)
    }

    data class Params(val phone: String)

}
