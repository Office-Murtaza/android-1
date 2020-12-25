package com.app.belcobtm.data.notification

import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.domain.notification.NotificationTokenRepository
import com.google.firebase.iid.FirebaseInstanceId
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class NotificationTokenRepositoryImpl(
    private val sharedPreferencesHelper: SharedPreferencesHelper
) : NotificationTokenRepository {

    override suspend fun getToken(): String {
        return sharedPreferencesHelper.notificationToken.orEmpty()
            .ifEmpty { loadNotificationToken() }
    }

    override fun saveToken(token: String) {
        sharedPreferencesHelper.notificationToken = token
    }

    private suspend fun loadNotificationToken(): String = suspendCoroutine { continuation ->
        FirebaseInstanceId.getInstance().instanceId
            .addOnSuccessListener {
                continuation.resume(it.token)
            }.addOnFailureListener {
                continuation.resume("")
            }
    }
}