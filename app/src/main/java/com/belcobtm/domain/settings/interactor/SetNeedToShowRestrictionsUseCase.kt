package com.belcobtm.domain.settings.interactor

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.settings.SettingsRepository

class SetNeedToShowRestrictionsUseCase(
    private val settingsRepository: SettingsRepository
) : UseCase<Unit, SetNeedToShowRestrictionsUseCase.Params>() {

    override suspend fun run(params: Params): Either<Failure, Unit> {
        return Either.Right(settingsRepository.setNeedToShowRestrictions(params.ifNeed))
    }

    data class Params(val ifNeed: Boolean)
}
