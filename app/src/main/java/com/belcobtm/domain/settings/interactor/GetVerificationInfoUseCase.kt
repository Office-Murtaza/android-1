package com.belcobtm.domain.settings.interactor

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.settings.SettingsRepository
import com.belcobtm.domain.settings.item.VerificationInfoDataItem

class GetVerificationInfoUseCase(private val repository: SettingsRepository) :
    UseCase<VerificationInfoDataItem, Unit>() {
    override suspend fun run(params: Unit): Either<Failure, VerificationInfoDataItem> = repository.getVerificationInfo()
}