package com.belcobtm.domain.settings.interactor

import android.content.Context
import android.graphics.BitmapFactory
import com.belcobtm.data.cloud.storage.CloudStorage
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.settings.SettingsRepository
import com.belcobtm.domain.settings.item.VerificationIdentityDataItem
import com.belcobtm.domain.settings.item.VerificationVipDataItem

class SendVerificationIdentityUseCase(
    private val repositoryImpl: SettingsRepository,
) : UseCase<Unit, SendVerificationIdentityUseCase.Params>() {

    override suspend fun run(params: Params): Either<Failure, Unit> {
        return repositoryImpl.sendVerificationIdentity(params.identityDataItem)
    }

    data class Params(val identityDataItem: VerificationIdentityDataItem)
}