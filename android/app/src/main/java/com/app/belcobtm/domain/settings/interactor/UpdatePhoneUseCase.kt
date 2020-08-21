package com.app.belcobtm.domain.settings.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.settings.SettingsRepository
const val ERROR_UPDATE_PHONE_IS_USED = 2
const val ERROR_UPDATE_PHONE_IS_SAME = 3

class UpdatePhoneUseCase(private val settingsRepository: SettingsRepository): UseCase<Boolean, UpdatePhoneUseCase.Params>() {

    override suspend fun run(params: Params): Either<Failure, Boolean> {
        return settingsRepository.updatePhone(params.phone)
    }

    data class Params(val phone: String)
}