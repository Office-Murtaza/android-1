package com.app.belcobtm.domain.settings

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.settings.item.VerificationBlankDataItem
import com.app.belcobtm.domain.settings.item.VerificationCountryDataItem
import com.app.belcobtm.domain.settings.item.VerificationInfoDataItem
import com.app.belcobtm.domain.settings.item.VerificationVipDataItem

interface SettingsRepository {
    suspend fun getVerificationInfo(): Either<Failure, VerificationInfoDataItem>
    suspend fun sendVerificationBlank(blankDataItem: VerificationBlankDataItem): Either<Failure, Unit>
    fun getVerificationCountries(): List<VerificationCountryDataItem>
    suspend fun sendVerificationVip(vipDataItem: VerificationVipDataItem): Either<Failure, Unit>
}