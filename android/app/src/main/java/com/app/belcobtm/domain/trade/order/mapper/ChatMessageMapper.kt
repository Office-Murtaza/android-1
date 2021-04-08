package com.app.belcobtm.domain.trade.order.mapper

import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.data.websockets.chat.model.ChatMessageResponse
import com.app.belcobtm.presentation.features.wallet.trade.order.chat.model.ChatMessageItem
import java.text.SimpleDateFormat
import java.util.*

class ChatMessageMapper(
    private val sharedPreferencesHelper: SharedPreferencesHelper,
    private val dateFormat: SimpleDateFormat
) {

    fun map(message: ChatMessageResponse): ChatMessageItem {
        val messageType = if (message.fromUserId == sharedPreferencesHelper.userId) {
            ChatMessageItem.MY_MESSAGE_TYPE
        } else {
            ChatMessageItem.PARTNER_MESSAGE_TYPE
        }
        return ChatMessageItem(
            message.message, dateFormat.format(Date(message.timestamp)), type = messageType
        )
    }
}