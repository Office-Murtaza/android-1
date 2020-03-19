package com.app.belcobtm.domain.settings.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.settings.SettingsRepository
import com.app.belcobtm.domain.settings.item.VerificationInfoDataItem

class GetVerificationInfoUseCase(private val repository: SettingsRepository) :
    UseCase<VerificationInfoDataItem, Unit>() {
    override suspend fun run(params: Unit): Either<Failure, VerificationInfoDataItem> = repository.getVerificationInfo()
}