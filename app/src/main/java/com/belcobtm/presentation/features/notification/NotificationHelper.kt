package com.belcobtm.presentation.features.notification

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
import com.belcobtm.R
import com.belcobtm.presentation.core.provider.string.StringProvider


class NotificationHelper(
    private val stringProvider: StringProvider,
    private val base: Context
) {

    companion object {
        const val PRIMARY_CHANNEL = "default"

        const val TITLE_KEY = "title"
        const val MESSAGE_KEY = "message"
        const val DEEPLINK_ID_KEY = "id"
        const val DEEPLINK_TYPE_KEY = "type"
        const val DEEPLINK_TYPE_TRADE = 1
        const val DEEPLINK_TYPE_ORDER = 2
        const val DEEPLINK_TYPE_CHAT = 3
        const val DEEPLINK_TYPE_TRANSACTIONS = 4
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

    fun resolveDeeplink(deeplinkType: String?, deeplinkId: String?): Uri? {
        if (deeplinkId.isNullOrEmpty()) {
            return null
        }
        val link = when (deeplinkType?.toIntOrNull()) {
            DEEPLINK_TYPE_CHAT ->
                stringProvider.getString(
                    R.string.trade_container_deeplink_format,
                    stringProvider.getString(R.string.chat_deeplink_format, deeplinkId)
                )
            DEEPLINK_TYPE_ORDER ->
                stringProvider.getString(
                    R.string.trade_container_deeplink_format,
                    stringProvider.getString(R.string.order_details_deeplink_format, deeplinkId)
                )
            DEEPLINK_TYPE_TRADE ->
                stringProvider.getString(
                    R.string.trade_container_deeplink_format,
                    stringProvider.getString(R.string.trade_details_deeplink_format, deeplinkId)
                )
            DEEPLINK_TYPE_TRANSACTIONS ->
                stringProvider.getString(R.string.transactions_deeplink_format, deeplinkId)
            else -> null
        }
        return link?.let(Uri::parse)
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