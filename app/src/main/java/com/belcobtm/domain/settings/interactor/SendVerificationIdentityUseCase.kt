package com.belcobtm.domain.settings.interactor

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.settings.SettingsRepository
import com.belcobtm.domain.settings.item.VerificationIdentityDataItem
import com.belcobtm.domain.settings.item.VerificationIdentityResponseDataItem

class SendVerificationIdentityUseCase(
    private val repositoryImpl: SettingsRepository,
) : UseCase<VerificationIdentityResponseDataItem, SendVerificationIdentityUseCase.Params>() {

    override suspend fun run(params: Params): Either<Failure, VerificationIdentityResponseDataItem> {
        return repositoryImpl.sendVerificationIdentity(params.identityDataItem)
    }

    data class Params(val identityDataItem: VerificationIdentityDataItem)
}