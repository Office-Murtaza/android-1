package com.belcobtm.domain.settings.interactor

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.settings.SettingsRepository
import com.belcobtm.domain.settings.item.VerificationFieldsDataItem

class GetVerificationFieldsUseCase(private val repository: SettingsRepository) :
    UseCase<VerificationFieldsDataItem, GetVerificationFieldsUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, VerificationFieldsDataItem> =
        repository.getVerificationFields(params.countryCode)

    data class Params(val countryCode: String)
}