package com.belcobtm.domain.settings.interactor

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.settings.SettingsRepository

const val CHANGE_PASS_ERROR_OLD_PASS = 2

class ChangePassUseCase(private val settingsRepository: SettingsRepository): UseCase<Boolean, ChangePassUseCase.Params>() {

    override suspend fun run(params: Params): Either<Failure, Boolean> {
        return settingsRepository.changePass(params.oldPassword, params.newPassword)
    }

    data class Params(val oldPassword: String, val newPassword: String)
}