package com.belcobtm.data.notification

import com.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.belcobtm.domain.notification.NotificationTokenRepository
import com.google.firebase.messaging.FirebaseMessaging
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
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener {
                continuation.resume(it)
            }.addOnFailureListener {
                continuation.resume("")
            }
    }

}
