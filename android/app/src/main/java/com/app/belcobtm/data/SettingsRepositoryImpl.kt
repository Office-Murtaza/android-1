package com.app.belcobtm.data

import com.app.belcobtm.data.core.NetworkUtils
import com.app.belcobtm.data.disk.AssetsDataStore
import com.app.belcobtm.data.rest.settings.SettingsApiService
import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.settings.SettingsRepository
import com.app.belcobtm.domain.settings.item.VerificationBlankDataItem
import com.app.belcobtm.domain.settings.item.VerificationCountryDataItem
import com.app.belcobtm.domain.settings.item.VerificationInfoDataItem
import com.app.belcobtm.domain.settings.item.VerificationVipDataItem
import com.app.belcobtm.domain.settings.type.VerificationStatus

class SettingsRepositoryImpl(
    private val apiService: SettingsApiService,
    private val assetsDataStore: AssetsDataStore,
    private val prefHelper: SharedPreferencesHelper,
    private val networkUtils: NetworkUtils
) : SettingsRepository {
    override suspend fun getVerificationInfo(): Either<Failure, VerificationInfoDataItem> =
        if (networkUtils.isNetworkAvailable()) {
            val response = apiService.getVerificationInfo(prefHelper.userId)
            if (response.isRight) {
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
        } else {
            Either.Left(Failure.NetworkConnection)
        }

    override suspend fun sendVerificationBlank(
        blankDataItem: VerificationBlankDataItem
    ): Either<Failure, Unit> = if (networkUtils.isNetworkAvailable()) {
        apiService.sendVerificationBlank(prefHelper.userId, blankDataItem)
    } else {
        Either.Left(Failure.NetworkConnection)
    }

    override fun getVerificationCountries(): List<VerificationCountryDataItem> = assetsDataStore.getCountries()

    override suspend fun sendVerificationVip(
        vipDataItem: VerificationVipDataItem
    ): Either<Failure, Unit> = if (networkUtils.isNetworkAvailable()) {
        apiService.sendVerificationVip(prefHelper.userId, vipDataItem)
    } else {
        Either.Left(Failure.NetworkConnection)
    }

    override suspend fun unlink(uesrId: String): Either<Failure, Boolean> =
        if (networkUtils.isNetworkAvailable()) {
            apiService.unlink(prefHelper.userId)
        } else {
            Either.Left(Failure.NetworkConnection)
        }
}