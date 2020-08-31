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
    suspend fun unlink(): Either<Failure, Boolean>
    suspend fun getPhone(): Either<Failure, String>
    suspend fun changePass(oldPassword: String, newPassword: String): Either<Failure, Boolean>
    suspend fun updatePhone(phone: String): Either<Failure, Boolean>
    suspend fun verifyPhone(phone: String): Either<Failure, Boolean>
}