package com.app.belcobtm.domain.settings.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.settings.SettingsRepository
import com.app.belcobtm.domain.settings.item.VerificationBlankDataItem

class SendVerificationBlankUseCase(private val repositoryImpl: SettingsRepository) :
    UseCase<Unit, SendVerificationBlankUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, Unit> = repositoryImpl.sendVerificationBlank(params.blankItem)

    data class Params(val blankItem: VerificationBlankDataItem)
}