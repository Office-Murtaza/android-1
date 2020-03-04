package com.app.belcobtm.domain.settings

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.settings.item.VerificationInfoDataItem

interface SettingsRepository {
    suspend fun getVerificationInfo(): Either<Failure, VerificationInfoDataItem>
}