package com.belcobtm.domain.trade.order.mapper

import android.content.Context
import com.belcobtm.data.cloud.auth.CloudAuth
import com.belcobtm.data.cloud.storage.CloudStorage
import com.belcobtm.data.websockets.chat.model.ChatMessageResponse
import com.belcobtm.domain.PreferencesInteractor
import com.belcobtm.presentation.core.DateFormat.getUserSelectedTimeFormat
import com.belcobtm.presentation.screens.wallet.trade.order.chat.model.ChatMessageItem
import com.google.firebase.crashlytics.FirebaseCrashlytics
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class ChatMessageMapper(
    private val preferences: PreferencesInteractor,
    private val cloudStorage: CloudStorage,
    private val cloudAuth: CloudAuth,
    private val dateFormat: SimpleDateFormat,
    private val context: Context
) {

    suspend fun map(message: ChatMessageResponse, isFromHistory: Boolean): ChatMessageItem {
        val messageType = if (message.fromUserId == preferences.userId) {
            ChatMessageItem.MY_MESSAGE_TYPE
        } else {
            ChatMessageItem.PARTNER_MESSAGE_TYPE
        }
        return ChatMessageItem(
            text = message.message ?: "",
            time = getFormattedDateTime(message.timestamp),
            timestamp = message.timestamp ?: 0L,
            imageUrl = message.file?.takeIf { it.isNotEmpty() }?.let {
                try {
                    getLink(it)
                } catch (e: Exception) {
                    FirebaseCrashlytics.getInstance().recordException(
                        RuntimeException("Failed to load $it", e)
                    )
                    null
                }
            },
            isFromHistory = isFromHistory,
            type = messageType
        )
    }

    private fun getFormattedDateTime(timestamp: Long?): String {
        // will give "18 Jul 2022, 07:15 PM" or "..., 19:15" depending on user's device setting
        val date = Date(timestamp ?: Calendar.getInstance().timeInMillis)
        return dateFormat.format(date) +
            getUserSelectedTimeFormat(context, date)
    }

    private suspend fun getLink(filename: String): String {
        if (!cloudAuth.currentUserExists()) {
            cloudAuth.authWithToken(preferences.firebaseToken)
        }
        return cloudStorage.getLink(filename)
    }

}
