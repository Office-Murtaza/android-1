package com.app.belcobtm.data.websockets.chat

import com.app.belcobtm.presentation.features.wallet.trade.order.chat.NewMessageItem

interface ChatObserver {

    fun connect()

    fun sendMessage(messageItem: NewMessageItem)

    fun disconnect()
}