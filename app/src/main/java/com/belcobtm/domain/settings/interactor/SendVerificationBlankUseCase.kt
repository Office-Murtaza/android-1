package com.belcobtm.domain.settings.interactor

import android.content.Context
import android.graphics.BitmapFactory
import com.belcobtm.data.cloud.storage.CloudStorage
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.settings.SettingsRepository
import com.belcobtm.domain.settings.item.VerificationBlankDataItem

class SendVerificationBlankUseCase(
    private val repositoryImpl: SettingsRepository,
    private val context: Context,
    private val cloudStorage: CloudStorage
) : UseCase<Unit, SendVerificationBlankUseCase.Params>() {

    companion object {
        const val FILE_EXTENSION = "jpg"
    }

    override suspend fun run(params: Params): Either<Failure, Unit> {
        val bitmap = BitmapFactory.decodeStream(
            context.contentResolver.openInputStream(params.blankItem.imageUri)
        )
        val fileName = "id${params.blankItem.idNumber}.$FILE_EXTENSION"
        cloudStorage.uploadBitmap(fileName, bitmap)
        return repositoryImpl.sendVerificationBlank(params.blankItem, fileName)
    }

    data class Params(val blankItem: VerificationBlankDataItem)
}