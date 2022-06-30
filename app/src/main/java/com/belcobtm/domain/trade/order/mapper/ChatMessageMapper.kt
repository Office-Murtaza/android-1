package com.belcobtm.domain.trade.order.mapper

import com.belcobtm.data.cloud.auth.CloudAuth
import com.belcobtm.data.cloud.storage.CloudStorage
import com.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.belcobtm.data.websockets.chat.model.ChatMessageResponse
import com.belcobtm.presentation.features.wallet.trade.order.chat.model.ChatMessageItem
import com.google.firebase.crashlytics.FirebaseCrashlytics
import java.text.SimpleDateFormat
import java.util.Date

class ChatMessageMapper(
    private val sharedPreferencesHelper: SharedPreferencesHelper,
    private val cloudStorage: CloudStorage,
    private val cloudAuth: CloudAuth,
    private val dateFormat: SimpleDateFormat
) {

    suspend fun map(message: ChatMessageResponse, isFromHistory: Boolean): ChatMessageItem {
        val messageType = if (message.fromUserId == sharedPreferencesHelper.userId) {
            ChatMessageItem.MY_MESSAGE_TYPE
        } else {
            ChatMessageItem.PARTNER_MESSAGE_TYPE
        }
        return ChatMessageItem(
            message.message, dateFormat.format(Date(message.timestamp)), message.timestamp,
            message.file?.takeIf { it.isNotEmpty() }?.let {
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

    private suspend fun getLink(filename: String): String {
        if (!cloudAuth.currentUserExists()) {
            cloudAuth.authWithToken(sharedPreferencesHelper.firebaseToken)
        }
        return cloudStorage.getLink(filename)
    }

}
