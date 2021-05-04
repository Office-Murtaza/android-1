package com.app.belcobtm.data.websockets.chat

import com.app.belcobtm.presentation.features.wallet.trade.order.chat.NewMessageItem

interface ChatObserver {

    suspend fun connect()

    suspend fun sendMessage(messageItem: NewMessageItem)

    suspend fun disconnect()
}