package com.app.belcobtm.domain.trade.order.mapper

import com.app.belcobtm.data.cloud.storage.CloudStorage
import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.data.websockets.chat.model.ChatMessageResponse
import com.app.belcobtm.presentation.features.wallet.trade.order.chat.model.ChatMessageItem
import java.text.SimpleDateFormat
import java.util.*

class ChatMessageMapper(
    private val sharedPreferencesHelper: SharedPreferencesHelper,
    private val cloudStorage: CloudStorage,
    private val dateFormat: SimpleDateFormat
) {

    suspend fun map(message: ChatMessageResponse): ChatMessageItem {
        val messageType = if (message.fromUserId == sharedPreferencesHelper.userId) {
            ChatMessageItem.MY_MESSAGE_TYPE
        } else {
            ChatMessageItem.PARTNER_MESSAGE_TYPE
        }
        return ChatMessageItem(
            message.message, dateFormat.format(Date(message.timestamp)),
            message.filename?.let { cloudStorage.getLink(it) },
            type = messageType
        )
    }
}