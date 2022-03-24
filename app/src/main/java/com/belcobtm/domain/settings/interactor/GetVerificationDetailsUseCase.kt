package com.belcobtm.domain.settings.interactor

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.settings.SettingsRepository
import com.belcobtm.domain.settings.item.VerificationDetailsDataItem
import com.belcobtm.domain.settings.item.VerificationInfoDataItem

class GetVerificationDetailsUseCase(private val repository: SettingsRepository) :
    UseCase<VerificationDetailsDataItem, Unit>() {
    override suspend fun run(params: Unit): Either<Failure, VerificationDetailsDataItem> = repository.getVerificationDetails()
}