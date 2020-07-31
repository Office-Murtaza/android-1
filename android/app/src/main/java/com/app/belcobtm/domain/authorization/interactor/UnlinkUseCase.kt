package com.app.belcobtm.domain.authorization.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.settings.SettingsRepository

class UnlinkUseCase(private val repository: SettingsRepository):
    UseCase<Boolean, Unit>() {

    override suspend fun run(params: Unit): Either<Failure, Boolean> {
        return repository.unlink()
    }
}