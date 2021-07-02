package com.app.belcobtm.domain.settings.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.settings.SettingsRepository

const val CHANGE_PASS_ERROR_OLD_PASS = 2

class ChangePassUseCase(private val settingsRepository: SettingsRepository): UseCase<Boolean, ChangePassUseCase.Params>() {

    override suspend fun run(params: Params): Either<Failure, Boolean> {
        return settingsRepository.changePass(params.oldPassword, params.newPassword)
    }

    data class Params(val oldPassword: String, val newPassword: String)
}