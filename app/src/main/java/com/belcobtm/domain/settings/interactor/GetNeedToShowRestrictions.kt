package com.belcobtm.domain.settings.interactor

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.settings.SettingsRepository

class GetNeedToShowRestrictions(private val repository: SettingsRepository) :
    UseCase<Boolean, Unit>() {
    override suspend fun run(params: Unit): Either<Failure, Boolean> = repository.needToShowRestrictions()
}