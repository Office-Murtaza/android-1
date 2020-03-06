package com.app.belcobtm.data

import com.app.belcobtm.data.core.NetworkUtils
import com.app.belcobtm.data.rest.settings.SettingsApiService
import com.app.belcobtm.data.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.settings.SettingsRepository
import com.app.belcobtm.domain.settings.item.VerificationInfoDataItem
import com.app.belcobtm.domain.settings.type.VerificationStatus

class SettingsRepositoryImpl(
    private val apiService: SettingsApiService,
    private val prefHelper: SharedPreferencesHelper,
    private val networkUtils: NetworkUtils
) : SettingsRepository {
    override suspend fun getVerificationInfo(): Either<Failure, VerificationInfoDataItem> =
        if (networkUtils.isNetworkAvailable()) {
            val response = apiService.getVerificationInfo(prefHelper.userId ?: -1)
            if (response.isRight) {
                val responseItem = (response as Either.Right).b
                Either.Right(
                    VerificationInfoDataItem(
                        status = VerificationStatus.getStatusByCode(responseItem.status),
                        txLimit = responseItem.txLimit,
                        dayLimit = responseItem.dailyLimit,
                        message = responseItem.message
                    )
                )
            } else {
                response as Either.Left
            }
        } else {
            Either.Left(Failure.NetworkConnection)
        }
}