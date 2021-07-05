package com.belcobtm.domain.settings.interactor

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.settings.SettingsRepository
const val ERROR_UPDATE_PHONE_IS_USED = 2
const val ERROR_UPDATE_PHONE_IS_SAME = 3

class UpdatePhoneUseCase(private val settingsRepository: SettingsRepository): UseCase<Boolean, UpdatePhoneUseCase.Params>() {

    override suspend fun run(params: Params): Either<Failure, Boolean> {
        return settingsRepository.updatePhone(params.phone)
    }

    data class Params(val phone: String)
}