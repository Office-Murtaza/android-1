package com.app.belcobtm.data.websockets.chat

import com.app.belcobtm.data.websockets.chat.model.ChatMessageResponse
import com.app.belcobtm.presentation.features.wallet.trade.order.chat.NewMessageItem
import kotlinx.coroutines.flow.Flow

interface ChatObserver {

    suspend fun connect()

    fun observeChatMessages(): Flow<List<ChatMessageResponse>>

    suspend fun sendMessage(messageItem: NewMessageItem)

    suspend fun disconnect()
}