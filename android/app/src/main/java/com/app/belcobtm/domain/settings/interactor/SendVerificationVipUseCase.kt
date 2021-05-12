package com.app.belcobtm.domain.settings.interactor

import android.content.Context
import android.graphics.BitmapFactory
import com.app.belcobtm.data.cloud.storage.CloudStorage
import com.app.belcobtm.data.core.RandomStringGenerator
import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.settings.SettingsRepository
import com.app.belcobtm.domain.settings.item.VerificationVipDataItem

class SendVerificationVipUseCase(
    private val repositoryImpl: SettingsRepository,
    private val context: Context,
    private val preferencesHelper: SharedPreferencesHelper,
    private val stringGenerator: RandomStringGenerator,
    private val cloudStorage: CloudStorage
) : UseCase<Unit, SendVerificationVipUseCase.Params>() {

    companion object {
        const val RANDOM_PART_SIZE = 10
    }

    override suspend fun run(params: Params): Either<Failure, Unit> {
        val bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(params.vipDataItem.fileUri))
        val fileName =
            "${preferencesHelper.userId}_snn_${stringGenerator.generate(RANDOM_PART_SIZE)}.jpg"
        cloudStorage.uploadBitmap(fileName, bitmap)
        return repositoryImpl.sendVerificationVip(params.vipDataItem, fileName)
    }

    data class Params(val vipDataItem: VerificationVipDataItem)
}