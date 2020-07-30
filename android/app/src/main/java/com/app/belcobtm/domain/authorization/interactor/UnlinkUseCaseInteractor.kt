package com.app.belcobtm.domain.authorization.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.settings.SettingsRepository

class UnlinkUseCase(private val repository: SettingsRepository):
    UseCase<Boolean, UnlinkUseCase.Params>() {

    override suspend fun run(params: Params): Either<Failure, Boolean> {
        return repository.unlink(params.userId)
    }

    data class Params(val userId: String)
}