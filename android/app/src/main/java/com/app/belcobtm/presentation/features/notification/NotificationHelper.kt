package com.app.belcobtm.presentation.features.notification

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.app.belcobtm.R


class NotificationHelper(private val base: Context) {

    companion object {
        const val PRIMARY_CHANNEL = "default"

        const val TITLE_KEY = "title"
        const val MESSAGE_KEY = "message"
    }

    private val isSdkMoreO = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

    private val smallIcon: Int
        get() = R.drawable.ic_notification

    private val uriSound: Uri
        get() = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

    init {
        if (isSdkMoreO) {
            createChannelIfNeed()
        }
    }

    fun getNotification(data: Map<String, String>, action: PendingIntent): Notification {
        val builder = if (isSdkMoreO) {
            NotificationCompat.Builder(base, PRIMARY_CHANNEL)
        } else {
            NotificationCompat.Builder(base)
                .setSound(uriSound)
        }
        with(builder) {
            setSmallIcon(smallIcon)
            setAutoCancel(true)
            setContentTitle(data[TITLE_KEY] ?: base.getString(R.string.app_name))
            setContentText(data[MESSAGE_KEY].orEmpty())
            setContentIntent(action)
        }
        return builder.build()
    }

    @SuppressLint("NewApi")
    private fun createChannelIfNeed() {
        with(base.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager) {
            if (getNotificationChannel(PRIMARY_CHANNEL) == null) {
                NotificationChannel(
                    PRIMARY_CHANNEL,
                    PRIMARY_CHANNEL,
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                    .apply { setSound(uriSound, Notification.AUDIO_ATTRIBUTES_DEFAULT) }
                    .also(::createNotificationChannel)
            }
        }
    }
}