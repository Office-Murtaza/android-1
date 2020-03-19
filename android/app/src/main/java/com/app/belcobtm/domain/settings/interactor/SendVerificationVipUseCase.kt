package com.app.belcobtm.domain.settings.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.settings.SettingsRepository
import com.app.belcobtm.domain.settings.item.VerificationBlankDataItem
import com.app.belcobtm.domain.settings.item.VerificationVipDataItem

class SendVerificationVipUseCase(private val repositoryImpl: SettingsRepository) :
    UseCase<Unit, SendVerificationVipUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, Unit> =
        repositoryImpl.sendVerificationVip(params.vipDataItem)

    data class Params(val vipDataItem: VerificationVipDataItem)
}