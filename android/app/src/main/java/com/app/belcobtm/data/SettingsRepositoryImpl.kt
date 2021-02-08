package com.app.belcobtm.data

import android.app.Application
import androidx.biometric.BiometricManager
import com.app.belcobtm.data.disk.AssetsDataStore
import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.data.rest.settings.SettingsApiService
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.settings.SettingsRepository
import com.app.belcobtm.domain.settings.item.VerificationBlankDataItem
import com.app.belcobtm.domain.settings.item.VerificationCountryDataItem
import com.app.belcobtm.domain.settings.item.VerificationInfoDataItem
import com.app.belcobtm.domain.settings.item.VerificationVipDataItem
import com.app.belcobtm.domain.settings.type.VerificationStatus

class SettingsRepositoryImpl(
    private val application: Application,
    private val apiService: SettingsApiService,
    private val assetsDataStore: AssetsDataStore,
    private val prefHelper: SharedPreferencesHelper
) : SettingsRepository {
    override suspend fun getVerificationInfo(): Either<Failure, VerificationInfoDataItem> {
        val response = apiService.getVerificationInfo(prefHelper.userId)
        return if (response.isRight) {
            val responseItem = (response as Either.Right).b
            Either.Right(
                VerificationInfoDataItem(
                    status = VerificationStatus.getStatusByCode(responseItem.status),
                    txLimit = responseItem.txLimit,
                    dayLimit = responseItem.dailyLimit,
                    message = responseItem.message ?: ""
                )
            )
        } else {
            response as Either.Left
        }
    }

    override suspend fun sendVerificationBlank(
        blankDataItem: VerificationBlankDataItem
    ): Either<Failure, Unit> = apiService.sendVerificationBlank(prefHelper.userId, blankDataItem)

    override fun getVerificationCountries(): List<VerificationCountryDataItem> =
        assetsDataStore.getCountries()

    override suspend fun sendVerificationVip(
        vipDataItem: VerificationVipDataItem
    ): Either<Failure, Unit> = apiService.sendVerificationVip(prefHelper.userId, vipDataItem)

    override suspend fun unlink(): Either<Failure, Boolean> = apiService.unlink(prefHelper.userId)

    override suspend fun changePass(
        oldPassword: String,
        newPassword: String
    ): Either<Failure, Boolean> = apiService.changePass(prefHelper.userId, oldPassword, newPassword)

    override suspend fun getPhone(): Either<Failure, String> =
        apiService.getPhone(prefHelper.userId)

    override suspend fun updatePhone(phone: String): Either<Failure, Boolean> =
        apiService.updatePhone(prefHelper.userId, phone)

    override suspend fun verifyPhone(phone: String): Either<Failure, Boolean> =
        apiService.verifyPhone(prefHelper.userId, phone)

    override suspend fun setUserAllowedBioAuth(allowed: Boolean) {
        prefHelper.userAllowedBioAuth = allowed
    }

    override suspend fun bioAuthSupportedByPhone(): Either<Failure, Boolean> {
        val bioManager = BiometricManager.from(application.applicationContext)
        val error = Either.Left(Failure.MessageError("BIOMETRIC_NOT_SUPPORTED"))
        return when (bioManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> Either.Right(true)
            else -> error
        }
    }

    override suspend fun userAllowedBioAuth(): Either<Failure, Boolean> {
        return Either.Right(prefHelper.userAllowedBioAuth)
    }
}
