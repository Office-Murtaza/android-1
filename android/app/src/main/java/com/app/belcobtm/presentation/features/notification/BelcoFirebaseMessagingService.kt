package com.app.belcobtm.presentation.features.notification

import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.presentation.features.HostActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.koin.android.ext.android.inject

class BelcoFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val NOTIFICATION_REQUEST_CODE = 777
    }

    private val notificationHelper: NotificationHelper by inject()

    private val sharedPresHelper: SharedPreferencesHelper by inject()

    override fun onNewToken(token: String) {
        sharedPresHelper.notificationToken = token
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        showNotification(remoteMessage.data)
    }

    private fun showNotification(data: Map<String, String>) {
        val pendingIntent = PendingIntent.getActivity(
            this,
            NOTIFICATION_REQUEST_CODE,
            Intent(this, HostActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NO_HISTORY
            },
            PendingIntent.FLAG_ONE_SHOT
        )

        NotificationManagerCompat.from(this)
            .notify(
                NOTIFICATION_ID,
                notificationHelper.getNotification(data, pendingIntent)
            )
    }
}