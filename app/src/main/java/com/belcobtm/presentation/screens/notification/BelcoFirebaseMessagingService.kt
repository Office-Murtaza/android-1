package com.belcobtm.presentation.screens.notification

import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.belcobtm.domain.notification.NotificationTokenRepository
import com.belcobtm.presentation.core.provider.string.StringProvider
import com.belcobtm.presentation.screens.HostActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.koin.android.ext.android.inject

class BelcoFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val NOTIFICATION_REQUEST_CODE = 777
    }

    private val notificationHelper: NotificationHelper by inject()

    private val notificationRepository: NotificationTokenRepository by inject()

    private val stringProvider: StringProvider by inject()

    override fun onNewToken(token: String) {
        notificationRepository.saveToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        showNotification(remoteMessage.data)
    }

    private fun showNotification(notificationData: Map<String, String>) {
        val pendingIntent = PendingIntent.getActivity(
            this,
            NOTIFICATION_REQUEST_CODE,
            Intent(this, HostActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NO_HISTORY
                data = notificationHelper.resolveDeeplink(
                    notificationData[NotificationHelper.DEEPLINK_TYPE_KEY],
                    notificationData[NotificationHelper.DEEPLINK_ID_KEY],
                )
            },
            PendingIntent.FLAG_ONE_SHOT
        )

        NotificationManagerCompat.from(this)
            .notify(
                NOTIFICATION_ID,
                notificationHelper.getNotification(notificationData, pendingIntent)
            )
    }
}