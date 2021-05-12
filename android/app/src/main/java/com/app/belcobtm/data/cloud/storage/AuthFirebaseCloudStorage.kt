package com.app.belcobtm.data.cloud.storage

import android.graphics.Bitmap
import com.app.belcobtm.data.cloud.auth.CloudAuth
import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper

class AuthFirebaseCloudStorage(
    private val sharedPreferencesHelper: SharedPreferencesHelper,
    private val cloudAuth: CloudAuth,
    private val cloudStorage: CloudStorage
) : CloudStorage {

    override suspend fun uploadBitmap(fileName: String, bitmap: Bitmap) =
        checkAuthAndPerform {
            cloudStorage.uploadBitmap(fileName, bitmap)
        }

    override suspend fun getLink(fileName: String): String =
        checkAuthAndPerform {
            cloudStorage.getLink(fileName)
        }

    private suspend inline fun <T> checkAuthAndPerform(block: () -> T): T {
        if (!cloudAuth.currentUserExists()) {
            cloudAuth.authWithToken(sharedPreferencesHelper.firebaseToken)
        }
        return block()
    }
}