package com.belcobtm.domain.settings.interactor

import android.content.Context
import com.belcobtm.data.cloud.storage.CloudStorage
import com.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.settings.SettingsRepository
import com.belcobtm.domain.settings.item.VerificationDocumentDataItem
import com.belcobtm.domain.settings.item.VerificationDocumentFirebaseImages
import com.belcobtm.domain.settings.item.VerificationDocumentResponseDataItem

class SendVerificationDocumentUseCase(
    private val repositoryImpl: SettingsRepository,
    private val context: Context,
    private val cloudStorage: CloudStorage,
    private val preferencesHelper: SharedPreferencesHelper

) : UseCase<VerificationDocumentResponseDataItem, SendVerificationDocumentUseCase.Params>() {

    companion object {
        const val FILE_EXTENSION = "jpg"
    }

    override suspend fun run(params: Params): Either<Failure, VerificationDocumentResponseDataItem> {

        val frontDocumentFileName =
            "${preferencesHelper.userId}_front_${params.documentItem.documentType.stringValue}_${System.currentTimeMillis()}.$FILE_EXTENSION"
        cloudStorage.uploadBitmap(frontDocumentFileName, params.documentItem.frontScanBitmap)

        val selfieFileName =
            "${preferencesHelper.userId}_selfie_${System.currentTimeMillis()}.$FILE_EXTENSION"
        cloudStorage.uploadBitmap(selfieFileName, params.documentItem.selfieBitmap)

        var backDocumentFileName: String? = null
        params.documentItem.backScanBitmap?.let {
            val fileName =
                "${preferencesHelper.userId}_back_${params.documentItem.documentType.stringValue}_${System.currentTimeMillis()}.$FILE_EXTENSION"
            cloudStorage.uploadBitmap(fileName, it)
            backDocumentFileName = fileName
        }

        return repositoryImpl.sendVerificationDocument(
            params.documentItem,
            VerificationDocumentFirebaseImages(
                frontDocumentFileName = frontDocumentFileName,
                backDocumentFileName = backDocumentFileName,
                selfieFileName = selfieFileName
            )
        )
    }

    data class Params(val documentItem: VerificationDocumentDataItem)
}